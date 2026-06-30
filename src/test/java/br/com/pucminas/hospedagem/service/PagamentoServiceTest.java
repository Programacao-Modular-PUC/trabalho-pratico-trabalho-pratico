package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.pagamento.PagamentoResponse;
import br.com.pucminas.hospedagem.dto.pagamento.PagarRequest;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Aluguel;
import br.com.pucminas.hospedagem.model.Pagamento;
import br.com.pucminas.hospedagem.model.enums.FormaPagamento;
import br.com.pucminas.hospedagem.model.enums.StatusPagamento;
import br.com.pucminas.hospedagem.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Aluguel aluguel;

    @BeforeEach
    void setUp() {
        aluguel = new Aluguel();
        aluguel.setId(10L);
    }

    private Pagamento novoPagamento(Long id, StatusPagamento status) {
        Pagamento pagamento = new Pagamento(aluguel, new BigDecimal("250.00"));
        pagamento.setId(id);
        pagamento.setStatus(status);
        return pagamento;
    }

    @Test
    void pagar_deveQuitarPagamentoPendente() {
        Pagamento pendente = novoPagamento(1L, StatusPagamento.PENDENTE);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pendente));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse response = pagamentoService.pagar(1L, new PagarRequest(FormaPagamento.PIX));

        assertEquals(StatusPagamento.PAGO, response.status());
        assertEquals(FormaPagamento.PIX, response.formaPagamento());
        assertNotNull(response.dataPagamento(), "dataPagamento deve ser preenchida ao quitar");
        assertEquals(10L, response.aluguelId());

        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(captor.capture());
        Pagamento salvo = captor.getValue();
        assertEquals(StatusPagamento.PAGO, salvo.getStatus());
        assertEquals(FormaPagamento.PIX, salvo.getFormaPagamento());
        assertNotNull(salvo.getDataPagamento());
    }

    @Test
    void pagar_pagamentoJaPago_deveLancarBusinessException() {
        Pagamento pago = novoPagamento(2L, StatusPagamento.PAGO);
        when(pagamentoRepository.findById(2L)).thenReturn(Optional.of(pago));

        assertThrows(BusinessException.class,
                () -> pagamentoService.pagar(2L, new PagarRequest(FormaPagamento.DINHEIRO)));

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void cancelar_deveMudarStatusParaCancelado() {
        Pagamento pendente = novoPagamento(3L, StatusPagamento.PENDENTE);
        when(pagamentoRepository.findById(3L)).thenReturn(Optional.of(pendente));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse response = pagamentoService.cancelar(3L);

        assertEquals(StatusPagamento.CANCELADO, response.status());

        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(captor.capture());
        assertEquals(StatusPagamento.CANCELADO, captor.getValue().getStatus());
    }

    @Test
    void buscarPorId_inexistente_deveLancarResourceNotFoundException() {
        when(pagamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagamentoService.buscarPorId(99L));
    }

    @Test
    void buscarPorAluguel_inexistente_deveLancarResourceNotFoundException() {
        when(pagamentoRepository.findByAluguelId(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagamentoService.buscarPorAluguel(77L));
    }
}
