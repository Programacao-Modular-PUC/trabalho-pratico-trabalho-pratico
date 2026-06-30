package br.com.pucminas.hospedagem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao (ponta a ponta) dos endpoints de cliente e autenticacao,
 * exercitando controller + service + repositorio sobre o banco H2.
 *
 * <p>Cada metodo roda em uma transacao revertida ao final ({@code @Transactional}),
 * garantindo isolamento entre os testes.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> clienteValido(String email) {
        Map<String, Object> cliente = new HashMap<>();
        cliente.put("nome", "Maria Souza");
        cliente.put("cpf", "529.982.247-25");
        cliente.put("endereco", "Rua das Conchas, 45");
        cliente.put("telefone", "(73) 98888-2222");
        cliente.put("email", email);
        cliente.put("senha", "segredo123");
        return cliente;
    }

    @Test
    @DisplayName("Cadastra cliente sem expor a senha e autentica com sucesso")
    void cadastrarEAutenticar() throws Exception {
        String email = "maria.integration@example.com";

        String corpoResposta = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(email))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertThat(corpoResposta).doesNotContain("segredo123");

        // Login correto -> 200
        Map<String, Object> loginOk = new HashMap<>();
        loginOk.put("email", email);
        loginOk.put("senha", "segredo123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginOk)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // Login com senha errada -> 400 (regra de negocio)
        Map<String, Object> loginErrado = new HashMap<>();
        loginErrado.put("email", email);
        loginErrado.put("senha", "senhaErrada");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginErrado)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rejeita cadastro com CPF invalido (400) e com email duplicado (400)")
    void rejeitaCpfInvalidoEEmailDuplicado() throws Exception {
        // CPF invalido (sequencia repetida) -> falha de validacao
        Map<String, Object> cpfInvalido = clienteValido("cpf.invalido@example.com");
        cpfInvalido.put("cpf", "111.111.111-11");
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cpfInvalido)))
                .andExpect(status().isBadRequest());

        // Cadastro valido
        String email = "duplicado@example.com";
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(email))))
                .andExpect(status().isCreated());

        // Mesmo email novamente -> 400 (regra de negocio)
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteValido(email))))
                .andExpect(status().isBadRequest());
    }
}
