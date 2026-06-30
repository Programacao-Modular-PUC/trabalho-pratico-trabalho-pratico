package br.com.pucminas.hospedagem.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integracao do fluxo principal do sistema: cadastro de cliente,
 * residencia e quarto, realizacao do aluguel (com calculo de diarias e valores),
 * emissao do recibo e bloqueio de periodo sobreposto.
 *
 * <p>Exercita toda a pilha (controller + service + regras + repositorio) sobre
 * o banco H2, com cada teste isolado por transacao revertida ao final.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AluguelFluxoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Periodo conhecido: entrada as 14h, saida 2 dias depois as 11h -> 2 diarias.
    private final LocalDateTime entrada = LocalDate.now().plusDays(1).atTime(14, 0);
    private final LocalDateTime saida = LocalDate.now().plusDays(3).atTime(11, 0);

    @Test
    @DisplayName("Realiza aluguel, calcula valores, gera pagamento e emite recibo")
    void fluxoCompletoDeAluguel() throws Exception {
        long clienteId = criarCliente("fluxo.aluguel@example.com");
        long residenciaId = criarResidencia();
        long quartoId = criarQuarto(residenciaId);

        // Antes de alugar, o quarto deve estar disponivel no periodo.
        mockMvc.perform(get("/api/quartos/{id}/disponibilidade", quartoId)
                        .param("dataEntrada", entrada.toString())
                        .param("dataSaida", saida.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true));

        // Realiza o aluguel.
        String respostaAluguel = mockMvc.perform(post("/api/alugueis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel(quartoId, clienteId, entrada, saida))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();

        JsonNode aluguelNode = objectMapper.readTree(respostaAluguel);
        // valor base 100.00 + ar-condicionado 30.00 = 130.00/diaria; 2 diarias = 260.00
        assertThat(aluguelNode.get("numeroDiarias").asInt()).isEqualTo(2);
        assertThat(aluguelNode.get("valorDiaria").asDouble()).isEqualTo(130.00);
        assertThat(aluguelNode.get("valorFinal").asDouble()).isEqualTo(260.00);
        long aluguelId = aluguelNode.get("id").asLong();

        // Emite o recibo.
        mockMvc.perform(get("/api/alugueis/{id}/recibo", aluguelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDiarias").value(2))
                .andExpect(jsonPath("$.totalPagar").value(260.00))
                .andExpect(jsonPath("$.textoFormatado").isNotEmpty());

        // Historico de alugueis da residencia.
        mockMvc.perform(get("/api/alugueis/residencia/{id}", residenciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Bloqueia aluguel de quarto ja ocupado em periodo sobreposto (409)")
    void naoPermiteAluguelComPeriodoSobreposto() throws Exception {
        long clienteId = criarCliente("conflito@example.com");
        long residenciaId = criarResidencia();
        long quartoId = criarQuarto(residenciaId);

        // Primeiro aluguel ocupa o periodo.
        mockMvc.perform(post("/api/alugueis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aluguel(quartoId, clienteId, entrada, saida))))
                .andExpect(status().isCreated());

        // Segundo aluguel em periodo sobreposto -> 409 (conflito).
        LocalDateTime entradaSobreposta = entrada.plusDays(1);
        LocalDateTime saidaSobreposta = saida.plusDays(1);
        mockMvc.perform(post("/api/alugueis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                aluguel(quartoId, clienteId, entradaSobreposta, saidaSobreposta))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Rejeita aluguel com data de entrada no passado (400)")
    void rejeitaAluguelComDataNoPassado() throws Exception {
        long clienteId = criarCliente("passado@example.com");
        long residenciaId = criarResidencia();
        long quartoId = criarQuarto(residenciaId);

        LocalDateTime entradaPassada = LocalDate.now().minusDays(2).atTime(14, 0);
        LocalDateTime saidaPassada = LocalDate.now().minusDays(1).atTime(11, 0);
        mockMvc.perform(post("/api/alugueis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                aluguel(quartoId, clienteId, entradaPassada, saidaPassada))))
                .andExpect(status().isBadRequest());
    }

    // ----------------------------------------------------------------------
    //  Auxiliares
    // ----------------------------------------------------------------------

    private Map<String, Object> aluguel(long quartoId, long clienteId,
                                        LocalDateTime entrada, LocalDateTime saida) {
        Map<String, Object> body = new HashMap<>();
        body.put("quartoId", quartoId);
        body.put("clienteId", clienteId);
        body.put("dataEntrada", entrada.toString());
        body.put("dataSaida", saida.toString());
        return body;
    }

    private long criarCliente(String email) throws Exception {
        Map<String, Object> cliente = new HashMap<>();
        cliente.put("nome", "Cliente Teste");
        cliente.put("cpf", "529.982.247-25");
        cliente.put("endereco", "Rua A, 1");
        cliente.put("telefone", "31999990000");
        cliente.put("email", email);
        cliente.put("senha", "segredo123");
        return postAndGetId("/api/clientes", cliente);
    }

    private long criarResidencia() throws Exception {
        Map<String, Object> residencia = new HashMap<>();
        residencia.put("endereco", "Avenida Beira-Mar");
        residencia.put("numero", "100");
        residencia.put("bairro", "Taipu de Fora");
        residencia.put("cep", "45520-000");
        residencia.put("telefone", "73999990000");
        residencia.put("email", "pousada@example.com");
        return postAndGetId("/api/residencias", residencia);
    }

    private long criarQuarto(long residenciaId) throws Exception {
        Map<String, Object> quarto = new HashMap<>();
        quarto.put("residenciaId", residenciaId);
        quarto.put("numero", "101");
        quarto.put("tipo", "INDIVIDUAL");
        quarto.put("valorBase", 100.00);
        quarto.put("temArCondicionado", true);
        quarto.put("temHidromassagem", false);
        quarto.put("descricao", "Quarto de teste");
        return postAndGetId("/api/quartos", quarto);
    }

    private long postAndGetId(String url, Object body) throws Exception {
        String resposta = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
