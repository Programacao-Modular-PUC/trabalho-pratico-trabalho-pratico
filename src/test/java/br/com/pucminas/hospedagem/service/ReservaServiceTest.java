package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.reserva.ReservaRequest;
import br.com.pucminas.hospedagem.dto.reserva.ReservaResponse;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.QuartoIndisponivelException;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Reserva;
import br.com.pucminas.hospedagem.model.enums.StatusReserva;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Testes unitarios de {@link ReservaService}, com os repositorios e o
 * {@link DisponibilidadeService} mockados (Mockito puro, sem contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private QuartoRepository quartoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DisponibilidadeService disponibilidadeService;

    @InjectMocks
    private ReservaService reservaService;

    private Quarto quarto;
    private Cliente cliente;
    private LocalDateTime entrada;
    private LocalDateTime saida;

    @BeforeEach
    void setUp() {
        quarto = new Quarto("101", TipoQuarto.CASAL, new BigDecimal("200.00"), true, false);
        quarto.setId(1L);

        cliente = new Cliente("Maria", "529.982.247-25", "Rua A, 10",
                "31999990000", "maria@example.com", "hash");
        cliente.setId(2L);

        entrada = LocalDate.now().plusDays(10).atTime(14, 0);
        saida = LocalDate.now().plusDays(12).atTime(12, 0);
    }

    @Test
    @DisplayName("criar deve salvar reserva PENDENTE quando quarto e cliente existem e periodo esta disponivel")
    void criarComSucesso() {
        ReservaRequest request = new ReservaRequest(1L, 2L, entrada, saida);

        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva r = invocation.getArgument(0);
            r.setId(99L);
            return r;
        });

        ReservaResponse response = reservaService.criar(request);

        // valida disponibilidade foi consultada com os dados corretos
        verify(disponibilidadeService).validarDisponibilidade(1L, entrada, saida);

        // a reserva persistida deve nascer PENDENTE e vincular quarto/cliente corretos
        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(captor.capture());
        Reserva salva = captor.getValue();
        assertThat(salva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(salva.getQuarto()).isSameAs(quarto);
        assertThat(salva.getCliente()).isSameAs(cliente);
        assertThat(salva.getDataEntrada()).isEqualTo(entrada);
        assertThat(salva.getDataSaida()).isEqualTo(saida);

        // resposta reflete a entidade salva
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.quartoId()).isEqualTo(1L);
        assertThat(response.quartoNumero()).isEqualTo("101");
        assertThat(response.clienteId()).isEqualTo(2L);
        assertThat(response.clienteNome()).isEqualTo("Maria");
        assertThat(response.status()).isEqualTo(StatusReserva.PENDENTE);
    }

    @Test
    @DisplayName("criar deve falhar com QuartoIndisponivelException quando o periodo esta ocupado")
    void criarComQuartoIndisponivel() {
        ReservaRequest request = new ReservaRequest(1L, 2L, entrada, saida);

        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        doThrow(new QuartoIndisponivelException(1L, entrada, saida))
                .when(disponibilidadeService).validarDisponibilidade(1L, entrada, saida);

        assertThatThrownBy(() -> reservaService.criar(request))
                .isInstanceOf(QuartoIndisponivelException.class);

        // nenhuma reserva deve ser persistida quando o quarto esta indisponivel
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("criar deve falhar com 404 quando o quarto nao existe (sem checar disponibilidade)")
    void criarComQuartoInexistente() {
        ReservaRequest request = new ReservaRequest(1L, 2L, entrada, saida);
        when(quartoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.criar(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(disponibilidadeService, never())
                .validarDisponibilidade(anyLong(), any(), any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("criar deve falhar quando a data de entrada esta no passado")
    void criarComDataNoPassado() {
        LocalDateTime entradaPassada = LocalDate.now().minusDays(1).atTime(14, 0);
        LocalDateTime saidaFutura = LocalDate.now().plusDays(1).atTime(11, 0);
        ReservaRequest request = new ReservaRequest(1L, 2L, entradaPassada, saidaFutura);

        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> reservaService.criar(request))
                .isInstanceOf(BusinessException.class);

        verify(disponibilidadeService, never())
                .validarDisponibilidade(anyLong(), any(), any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("confirmar deve falhar quando a reserva ja esta concluida")
    void confirmarReservaConcluida() {
        Reserva reserva = new Reserva(quarto, cliente, entrada, saida);
        reserva.setId(8L);
        reserva.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(8L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.confirmar(8L))
                .isInstanceOf(BusinessException.class);

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("cancelar deve mudar o status da reserva para CANCELADA")
    void cancelarMudaStatus() {
        Reserva reserva = new Reserva(quarto, cliente, entrada, saida);
        reserva.setId(5L);
        reserva.setStatus(StatusReserva.PENDENTE);

        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservaResponse response = reservaService.cancelar(5L);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(response.status()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(response.id()).isEqualTo(5L);
        verify(reservaRepository).save(reserva);
    }
}
