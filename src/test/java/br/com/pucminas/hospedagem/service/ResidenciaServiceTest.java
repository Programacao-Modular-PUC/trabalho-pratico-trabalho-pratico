package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.residencia.QuartoResumoResponse;
import br.com.pucminas.hospedagem.dto.residencia.ResidenciaRequest;
import br.com.pucminas.hospedagem.dto.residencia.ResidenciaResponse;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResidenciaService")
class ResidenciaServiceTest {

    @Mock
    private ResidenciaRepository residenciaRepository;

    @InjectMocks
    private ResidenciaService residenciaService;

    private ResidenciaRequest request;

    @BeforeEach
    void setUp() {
        request = new ResidenciaRequest(
                "Rua das Flores",
                "100",
                "Centro",
                "30100-000",
                "(31) 99999-0000",
                "contato@residencia.com"
        );
    }

    @Test
    @DisplayName("criar deve persistir a residencia e mapear os quartos no response")
    void criarComSucesso() {
        Quarto quarto = new Quarto("101", TipoQuarto.CASAL, new BigDecimal("250.00"), true, false);
        quarto.setId(7L);

        Residencia salva = new Residencia(
                request.endereco(), request.numero(), request.bairro(),
                request.cep(), request.telefone(), request.email());
        salva.setId(1L);
        salva.adicionarQuarto(quarto);

        when(residenciaRepository.save(any(Residencia.class))).thenReturn(salva);

        ResidenciaResponse response = residenciaService.criar(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.endereco()).isEqualTo("Rua das Flores");
        assertThat(response.numero()).isEqualTo("100");
        assertThat(response.bairro()).isEqualTo("Centro");
        assertThat(response.cep()).isEqualTo("30100-000");
        assertThat(response.telefone()).isEqualTo("(31) 99999-0000");
        assertThat(response.email()).isEqualTo("contato@residencia.com");

        assertThat(response.quartos()).hasSize(1);
        QuartoResumoResponse quartoResumo = response.quartos().get(0);
        assertThat(quartoResumo.id()).isEqualTo(7L);
        assertThat(quartoResumo.numero()).isEqualTo("101");
        assertThat(quartoResumo.tipo()).isEqualTo(TipoQuarto.CASAL);
        assertThat(quartoResumo.valorBase()).isEqualByComparingTo("250.00");

        ArgumentCaptor<Residencia> captor = ArgumentCaptor.forClass(Residencia.class);
        verify(residenciaRepository).save(captor.capture());
        Residencia enviada = captor.getValue();
        assertThat(enviada.getEndereco()).isEqualTo("Rua das Flores");
        assertThat(enviada.getEmail()).isEqualTo("contato@residencia.com");
    }

    @Test
    @DisplayName("buscarPorId deve lancar ResourceNotFoundException quando inexistente")
    void buscarPorIdInexistente() {
        when(residenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> residenciaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(residenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizar deve alterar os campos da residencia existente")
    void atualizarAlteraCampos() {
        Residencia existente = new Residencia(
                "Endereco Antigo", "1", "Bairro Antigo",
                "00000-000", "(31) 0000-0000", "antigo@email.com");
        existente.setId(5L);

        ResidenciaRequest atualizacao = new ResidenciaRequest(
                "Av. Nova", "200", "Savassi",
                "30130-000", "(31) 98888-7777", "novo@residencia.com");

        when(residenciaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(residenciaRepository.save(any(Residencia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResidenciaResponse response = residenciaService.atualizar(5L, atualizacao);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.endereco()).isEqualTo("Av. Nova");
        assertThat(response.numero()).isEqualTo("200");
        assertThat(response.bairro()).isEqualTo("Savassi");
        assertThat(response.cep()).isEqualTo("30130-000");
        assertThat(response.telefone()).isEqualTo("(31) 98888-7777");
        assertThat(response.email()).isEqualTo("novo@residencia.com");

        assertThat(existente.getEndereco()).isEqualTo("Av. Nova");
        assertThat(existente.getEmail()).isEqualTo("novo@residencia.com");

        verify(residenciaRepository).save(existente);
    }

    @Test
    @DisplayName("listar deve mapear todas as residencias")
    void listarTodas() {
        Residencia r1 = new Residencia("End 1", "1", "B1", "11111-000", "tel1", "e1@x.com");
        r1.setId(1L);
        Residencia r2 = new Residencia("End 2", "2", "B2", "22222-000", "tel2", "e2@x.com");
        r2.setId(2L);

        when(residenciaRepository.findAll()).thenReturn(List.of(r1, r2));

        List<ResidenciaResponse> responses = residenciaService.listar();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ResidenciaResponse::id).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("remover deve lancar ResourceNotFoundException quando inexistente")
    void removerInexistente() {
        when(residenciaRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> residenciaService.remover(42L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(residenciaRepository, never()).delete(any());
    }
}
