package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.quarto.DisponibilidadeResponse;
import br.com.pucminas.hospedagem.dto.quarto.QuartoRequest;
import br.com.pucminas.hospedagem.dto.quarto.QuartoResponse;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import br.com.pucminas.hospedagem.service.calculo.AdicionalArCondicionado;
import br.com.pucminas.hospedagem.service.calculo.AdicionalDiaria;
import br.com.pucminas.hospedagem.service.calculo.AdicionalHidromassagem;
import br.com.pucminas.hospedagem.service.calculo.CalculadoraDiaria;
import br.com.pucminas.hospedagem.service.factory.QuartoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios de {@link QuartoService} usando Mockito para os repositorios
 * e colaboradores. O {@link CalculadoraDiaria} e usado de forma real para validar
 * o calculo efetivo do valor da diaria.
 */
@ExtendWith(MockitoExtension.class)
class QuartoServiceTest {

    @Mock
    private QuartoRepository quartoRepository;

    @Mock
    private ResidenciaRepository residenciaRepository;

    @Mock
    private DisponibilidadeService disponibilidadeService;

    private QuartoFactory quartoFactory;
    private CalculadoraDiaria calculadoraDiaria;
    private QuartoService quartoService;

    @BeforeEach
    void setUp() {
        // Colaboradores "reais" sem estado externo: factory e calculadora.
        quartoFactory = new QuartoFactory();
        List<AdicionalDiaria> adicionais = List.of(
                new AdicionalArCondicionado(new BigDecimal("30.00")),
                new AdicionalHidromassagem(new BigDecimal("50.00")));
        calculadoraDiaria = new CalculadoraDiaria(adicionais);

        quartoService = new QuartoService(
                quartoRepository,
                residenciaRepository,
                quartoFactory,
                calculadoraDiaria,
                disponibilidadeService);
    }

    private Residencia residenciaComId(Long id) {
        Residencia residencia = new Residencia(
                "Rua das Flores", "100", "Centro", "30000-000",
                "31999999999", "contato@hotel.com");
        residencia.setId(id);
        return residencia;
    }

    @Test
    void criarComResidenciaExistenteDevePersistirEVincularResidencia() {
        Long residenciaId = 7L;
        Residencia residencia = residenciaComId(residenciaId);
        QuartoRequest request = new QuartoRequest(
                residenciaId, "101", TipoQuarto.CASAL,
                new BigDecimal("200.00"), true, false, "Vista para o mar");

        when(residenciaRepository.findById(residenciaId)).thenReturn(Optional.of(residencia));
        when(quartoRepository.save(any(Quarto.class))).thenAnswer(invocation -> {
            Quarto q = invocation.getArgument(0);
            q.setId(42L);
            return q;
        });

        QuartoResponse response = quartoService.criar(request);

        assertNotNull(response);
        assertEquals(42L, response.id());
        assertEquals(residenciaId, response.residenciaId());
        assertEquals("101", response.numero());
        assertEquals(TipoQuarto.CASAL, response.tipo());
        assertEquals(new BigDecimal("200.00"), response.valorBase());
        assertTrue(response.temArCondicionado());
        assertFalse(response.temHidromassagem());
        assertEquals("Vista para o mar", response.descricao());
        // 200.00 base + 30.00 ar-condicionado (sem hidromassagem) = 230.00
        assertEquals(new BigDecimal("230.00"), response.valorDiariaCalculado());

        ArgumentCaptor<Quarto> captor = ArgumentCaptor.forClass(Quarto.class);
        verify(quartoRepository).save(captor.capture());
        Quarto salvo = captor.getValue();
        assertNotNull(salvo.getResidencia());
        assertEquals(residenciaId, salvo.getResidencia().getId());
    }

    @Test
    void criarComResidenciaInexistenteDeveLancarResourceNotFound() {
        Long residenciaId = 999L;
        QuartoRequest request = new QuartoRequest(
                residenciaId, "101", TipoQuarto.INDIVIDUAL,
                new BigDecimal("100.00"), false, false, null);

        when(residenciaRepository.findById(residenciaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quartoService.criar(request));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void valorDiariaCalculadoDeveSomarArEHidromassagemAoValorBase() {
        Long residenciaId = 1L;
        Residencia residencia = residenciaComId(residenciaId);
        QuartoRequest request = new QuartoRequest(
                residenciaId, "201", TipoQuarto.CASAL,
                new BigDecimal("250.00"), true, true, "Suite premium");

        when(residenciaRepository.findById(residenciaId)).thenReturn(Optional.of(residencia));
        when(quartoRepository.save(any(Quarto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuartoResponse response = quartoService.criar(request);

        // 250.00 base + 30.00 ar-condicionado + 50.00 hidromassagem = 330.00
        assertEquals(new BigDecimal("330.00"), response.valorDiariaCalculado());
        assertTrue(response.temArCondicionado());
        assertTrue(response.temHidromassagem());
    }

    @Test
    void buscarPorIdInexistenteDeveLancarResourceNotFound() {
        when(quartoRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> quartoService.buscarPorId(123L));
    }

    @Test
    void verificarDisponibilidadeDeveConsultarServicoQuandoQuartoExiste() {
        Long quartoId = 5L;
        LocalDateTime entrada = LocalDateTime.of(2026, 7, 1, 12, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 7, 5, 12, 0);

        Quarto quarto = quartoFactory.criar(
                "301", TipoQuarto.INDIVIDUAL, new BigDecimal("120.00"), false, false, null);
        quarto.setId(quartoId);

        when(quartoRepository.findById(quartoId)).thenReturn(Optional.of(quarto));
        when(disponibilidadeService.estaDisponivel(quartoId, entrada, saida)).thenReturn(true);

        DisponibilidadeResponse response =
                quartoService.verificarDisponibilidade(quartoId, entrada, saida);

        assertEquals(quartoId, response.quartoId());
        assertEquals(entrada, response.dataEntrada());
        assertEquals(saida, response.dataSaida());
        assertTrue(response.disponivel());
        verify(disponibilidadeService).estaDisponivel(quartoId, entrada, saida);
    }

    @Test
    void verificarDisponibilidadeComQuartoInexistenteDeveLancarResourceNotFound() {
        Long quartoId = 404L;
        LocalDateTime entrada = LocalDateTime.of(2026, 7, 1, 12, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 7, 5, 12, 0);

        when(quartoRepository.findById(quartoId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> quartoService.verificarDisponibilidade(quartoId, entrada, saida));
        verify(disponibilidadeService, never()).estaDisponivel(anyLong(), any(), any());
    }

    @Test
    void removerInexistenteDeveLancarResourceNotFound() {
        when(quartoRepository.findById(50L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> quartoService.remover(50L));
        verify(quartoRepository, never()).delete(any());
    }

    @Test
    void atualizarDeveAlterarDadosDoQuartoExistente() {
        Long quartoId = 10L;
        Long residenciaId = 3L;
        Residencia residencia = residenciaComId(residenciaId);
        Quarto existente = quartoFactory.criar(
                "101", TipoQuarto.INDIVIDUAL, new BigDecimal("100.00"), false, false, "antigo");
        existente.setId(quartoId);

        QuartoRequest request = new QuartoRequest(
                residenciaId, "102", TipoQuarto.CASAL,
                new BigDecimal("180.00"), true, false, "novo");

        when(quartoRepository.findById(quartoId)).thenReturn(Optional.of(existente));
        when(residenciaRepository.findById(residenciaId)).thenReturn(Optional.of(residencia));
        when(quartoRepository.save(any(Quarto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuartoResponse response = quartoService.atualizar(quartoId, request);

        assertEquals(quartoId, response.id());
        assertEquals("102", response.numero());
        assertEquals(TipoQuarto.CASAL, response.tipo());
        assertEquals(new BigDecimal("180.00"), response.valorBase());
        assertEquals(residenciaId, response.residenciaId());
        // 180.00 + 30.00 ar-condicionado = 210.00
        assertEquals(new BigDecimal("210.00"), response.valorDiariaCalculado());
        verify(quartoRepository).save(eq(existente));
    }
}
