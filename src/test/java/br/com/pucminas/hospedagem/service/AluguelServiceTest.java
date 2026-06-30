package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.aluguel.AluguelRequest;
import br.com.pucminas.hospedagem.dto.aluguel.AluguelResponse;
import br.com.pucminas.hospedagem.dto.aluguel.ReciboResponse;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.QuartoIndisponivelException;
import br.com.pucminas.hospedagem.model.Aluguel;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.model.enums.StatusPagamento;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import br.com.pucminas.hospedagem.repository.AluguelRepository;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ReservaRepository;
import br.com.pucminas.hospedagem.service.calculo.AdicionalArCondicionado;
import br.com.pucminas.hospedagem.service.calculo.AdicionalHidromassagem;
import br.com.pucminas.hospedagem.service.calculo.CalculadoraDiaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link AluguelService}, mockando os repositorios e o
 * {@link DisponibilidadeService}, mas usando uma {@link CalculadoraDiaria} real
 * para validar o calculo de diarias e valores.
 */
@ExtendWith(MockitoExtension.class)
class AluguelServiceTest {

    @Mock
    private AluguelRepository aluguelRepository;
    @Mock
    private QuartoRepository quartoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private DisponibilidadeService disponibilidadeService;

    private CalculadoraDiaria calculadoraDiaria;
    private AluguelService aluguelService;

    // Datas relativas (sempre no futuro) preservando 2 diarias:
    // entrada as 14h e saida 2 dias depois as 11h (antes do meio-dia).
    private static final LocalDateTime ENTRADA = LocalDate.now().plusDays(1).atTime(14, 0);
    private static final LocalDateTime SAIDA = LocalDate.now().plusDays(3).atTime(11, 0);

    @BeforeEach
    void setUp() {
        calculadoraDiaria = new CalculadoraDiaria(List.of(
                new AdicionalArCondicionado(new BigDecimal("30.00")),
                new AdicionalHidromassagem(new BigDecimal("50.00"))));
        aluguelService = new AluguelService(aluguelRepository, quartoRepository,
                clienteRepository, reservaRepository, disponibilidadeService, calculadoraDiaria);
    }

    private Quarto quartoComArCondicionado() {
        Residencia residencia = new Residencia("Rua das Flores", "100", "Centro",
                "30100-000", "31999990000", "contato@host.com");
        residencia.setId(7L);

        Quarto quarto = new Quarto("101", TipoQuarto.INDIVIDUAL,
                new BigDecimal("100.00"), true, false);
        quarto.setId(1L);
        quarto.setResidencia(residencia);
        return quarto;
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente("Joao", "12345678909", "Rua A",
                "31988887777", "joao@email.com", "hash");
        cliente.setId(2L);
        return cliente;
    }

    @Test
    void realizar_calculaDiariasEValorFinalECriaPagamentoPendente() {
        Quarto quarto = quartoComArCondicionado();
        Cliente cliente = cliente();

        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(aluguelRepository.save(any(Aluguel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AluguelRequest request = new AluguelRequest(1L, 2L, ENTRADA, SAIDA, null);

        AluguelResponse response = aluguelService.realizar(request);

        // 2026-07-01T14:00 -> 2026-07-03T11:00 = 2 diarias (saida antes do meio-dia)
        assertThat(response.numeroDiarias()).isEqualTo(2L);
        // valor base 100.00 + ar-condicionado 30.00 (sem hidromassagem)
        assertThat(response.valorDiaria()).isEqualByComparingTo(new BigDecimal("130.00"));
        assertThat(response.valorFinal()).isEqualByComparingTo(new BigDecimal("260.00"));
        assertThat(response.statusPagamento()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(response.quartoId()).isEqualTo(1L);
        assertThat(response.clienteId()).isEqualTo(2L);
        assertThat(response.residenciaId()).isEqualTo(7L);
        assertThat(response.tipoQuarto()).isEqualTo(TipoQuarto.INDIVIDUAL);

        verify(disponibilidadeService).validarDisponibilidade(1L, ENTRADA, SAIDA, null);
        verify(aluguelRepository).save(any(Aluguel.class));
    }

    @Test
    void realizar_propagaQuartoIndisponivelException() {
        Quarto quarto = quartoComArCondicionado();
        Cliente cliente = cliente();

        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        doThrow(new QuartoIndisponivelException(1L, ENTRADA, SAIDA))
                .when(disponibilidadeService)
                .validarDisponibilidade(anyLong(), any(), any(), any());

        AluguelRequest request = new AluguelRequest(1L, 2L, ENTRADA, SAIDA, null);

        assertThatThrownBy(() -> aluguelService.realizar(request))
                .isInstanceOf(QuartoIndisponivelException.class);

        verify(aluguelRepository, never()).save(any(Aluguel.class));
    }

    @Test
    void realizar_falhaQuandoDataEntradaNoPassado() {
        Quarto quarto = quartoComArCondicionado();
        Cliente cliente = cliente();

        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));

        LocalDateTime entradaNoPassado = LocalDate.now().minusDays(2).atTime(14, 0);
        LocalDateTime saidaNoPassado = LocalDate.now().minusDays(1).atTime(11, 0);
        AluguelRequest request = new AluguelRequest(1L, 2L, entradaNoPassado, saidaNoPassado, null);

        assertThatThrownBy(() -> aluguelService.realizar(request))
                .isInstanceOf(BusinessException.class);

        verify(aluguelRepository, never()).save(any(Aluguel.class));
    }

    @Test
    void emitirRecibo_retornaTextoFormatadoNaoVazio() {
        Quarto quarto = quartoComArCondicionado();
        Cliente cliente = cliente();

        Aluguel aluguel = new Aluguel();
        aluguel.setId(5L);
        aluguel.setResidencia(quarto.getResidencia());
        aluguel.setQuarto(quarto);
        aluguel.setCliente(cliente);
        aluguel.setDataEntrada(ENTRADA);
        aluguel.setDataSaida(SAIDA);
        aluguel.setNumeroDiarias(2L);
        aluguel.setValorDiaria(new BigDecimal("130.00"));
        aluguel.setValorFinal(new BigDecimal("260.00"));

        when(aluguelRepository.findById(5L)).thenReturn(Optional.of(aluguel));

        ReciboResponse recibo = aluguelService.emitirRecibo(5L);

        assertThat(recibo.textoFormatado()).isNotBlank();
        assertThat(recibo.textoFormatado()).contains("RECIBO DE HOSPEDAGEM");
        assertThat(recibo.cliente()).isEqualTo("Joao");
        assertThat(recibo.numeroDiarias()).isEqualTo(2L);
        assertThat(recibo.totalPagar()).isEqualByComparingTo(new BigDecimal("260.00"));
    }
}
