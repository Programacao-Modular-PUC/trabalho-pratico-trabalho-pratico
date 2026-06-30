package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.relatorio.FaturamentoResponse;
import br.com.pucminas.hospedagem.dto.relatorio.QuartoDisponivelResponse;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Aluguel;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import br.com.pucminas.hospedagem.repository.AluguelRepository;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import br.com.pucminas.hospedagem.service.calculo.AdicionalArCondicionado;
import br.com.pucminas.hospedagem.service.calculo.AdicionalHidromassagem;
import br.com.pucminas.hospedagem.service.calculo.CalculadoraDiaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link RelatorioService} (Mockito puro), usando uma
 * {@link CalculadoraDiaria} real para o calculo do valor da diaria.
 */
@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private AluguelRepository aluguelRepository;
    @Mock
    private ResidenciaRepository residenciaRepository;
    @Mock
    private QuartoRepository quartoRepository;
    @Mock
    private DisponibilidadeService disponibilidadeService;

    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        CalculadoraDiaria calculadoraDiaria = new CalculadoraDiaria(List.of(
                new AdicionalArCondicionado(new BigDecimal("30.00")),
                new AdicionalHidromassagem(new BigDecimal("50.00"))));
        relatorioService = new RelatorioService(aluguelRepository, residenciaRepository,
                quartoRepository, disponibilidadeService, calculadoraDiaria);
    }

    @Test
    void faturamento_somaOsValoresFinaisDosAlugueis() {
        Residencia residencia = new Residencia("Av. Beira-Mar", "100", "Taipu",
                "45520-000", "73999990000", "pousada@example.com");
        residencia.setId(1L);
        when(residenciaRepository.findById(1L)).thenReturn(Optional.of(residencia));

        Aluguel a1 = new Aluguel();
        a1.setValorFinal(new BigDecimal("260.00"));
        Aluguel a2 = new Aluguel();
        a2.setValorFinal(new BigDecimal("360.00"));
        when(aluguelRepository.findByResidenciaId(1L)).thenReturn(List.of(a1, a2));

        FaturamentoResponse resposta = relatorioService.faturamentoPorResidencia(1L);

        assertThat(resposta.residenciaId()).isEqualTo(1L);
        assertThat(resposta.quantidadeAlugueis()).isEqualTo(2);
        assertThat(resposta.valorTotal()).isEqualByComparingTo(new BigDecimal("620.00"));
    }

    @Test
    void faturamento_residenciaInexistenteLancaNotFound() {
        when(residenciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> relatorioService.faturamentoPorResidencia(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void quartosDisponiveis_retornaApenasOsLivresComValorDaDiaria() {
        Quarto q1 = new Quarto("101", TipoQuarto.INDIVIDUAL, new BigDecimal("100.00"), true, false);
        q1.setId(1L);
        Quarto q2 = new Quarto("102", TipoQuarto.CASAL, new BigDecimal("200.00"), false, false);
        q2.setId(2L);
        when(quartoRepository.findAll()).thenReturn(List.of(q1, q2));

        LocalDateTime entrada = LocalDateTime.now().plusDays(1);
        LocalDateTime saida = LocalDateTime.now().plusDays(3);
        when(disponibilidadeService.estaDisponivel(1L, entrada, saida)).thenReturn(true);
        when(disponibilidadeService.estaDisponivel(2L, entrada, saida)).thenReturn(false);

        List<QuartoDisponivelResponse> disponiveis =
                relatorioService.quartosDisponiveis(entrada, saida, null);

        assertThat(disponiveis).hasSize(1);
        assertThat(disponiveis.get(0).quartoId()).isEqualTo(1L);
        // valor base 100.00 + ar-condicionado 30.00
        assertThat(disponiveis.get(0).valorDiaria()).isEqualByComparingTo(new BigDecimal("130.00"));
    }
}
