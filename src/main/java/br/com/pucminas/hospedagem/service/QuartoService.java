package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.quarto.DisponibilidadeResponse;
import br.com.pucminas.hospedagem.dto.quarto.QuartoRequest;
import br.com.pucminas.hospedagem.dto.quarto.QuartoResponse;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import br.com.pucminas.hospedagem.service.calculo.CalculadoraDiaria;
import br.com.pucminas.hospedagem.service.factory.QuartoFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servico de aplicacao para a gestao de quartos (requisitos 1 e 2).
 *
 * <p>Orquestra a criacao via {@link QuartoFactory}, o calculo do valor da diaria
 * via {@link CalculadoraDiaria} e a verificacao de disponibilidade via
 * {@link DisponibilidadeService}.</p>
 */
@Service
public class QuartoService {

    private final QuartoRepository quartoRepository;
    private final ResidenciaRepository residenciaRepository;
    private final QuartoFactory quartoFactory;
    private final CalculadoraDiaria calculadoraDiaria;
    private final DisponibilidadeService disponibilidadeService;

    public QuartoService(QuartoRepository quartoRepository,
                         ResidenciaRepository residenciaRepository,
                         QuartoFactory quartoFactory,
                         CalculadoraDiaria calculadoraDiaria,
                         DisponibilidadeService disponibilidadeService) {
        this.quartoRepository = quartoRepository;
        this.residenciaRepository = residenciaRepository;
        this.quartoFactory = quartoFactory;
        this.calculadoraDiaria = calculadoraDiaria;
        this.disponibilidadeService = disponibilidadeService;
    }

    @Transactional
    public QuartoResponse criar(QuartoRequest request) {
        Residencia residencia = buscarResidencia(request.residenciaId());

        Quarto quarto = quartoFactory.criar(
                request.numero(),
                request.tipo(),
                request.valorBase(),
                request.temArCondicionado(),
                request.temHidromassagem(),
                request.descricao());
        quarto.setResidencia(residencia);

        Quarto salvo = quartoRepository.save(quarto);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<QuartoResponse> listar() {
        return quartoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuartoResponse> listarPorResidencia(Long residenciaId) {
        return quartoRepository.findByResidenciaId(residenciaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuartoResponse buscarPorId(Long id) {
        return toResponse(buscarQuarto(id));
    }

    @Transactional
    public QuartoResponse atualizar(Long id, QuartoRequest request) {
        Quarto quarto = buscarQuarto(id);
        Residencia residencia = buscarResidencia(request.residenciaId());

        quarto.setNumero(request.numero());
        quarto.setTipo(request.tipo());
        quarto.setValorBase(request.valorBase());
        quarto.setTemArCondicionado(request.temArCondicionado());
        quarto.setTemHidromassagem(request.temHidromassagem());
        quarto.setDescricao(request.descricao());
        quarto.setResidencia(residencia);

        Quarto salvo = quartoRepository.save(quarto);
        return toResponse(salvo);
    }

    @Transactional
    public void remover(Long id) {
        Quarto quarto = buscarQuarto(id);
        quartoRepository.delete(quarto);
    }

    @Transactional(readOnly = true)
    public DisponibilidadeResponse verificarDisponibilidade(Long quartoId,
                                                            LocalDateTime entrada,
                                                            LocalDateTime saida) {
        buscarQuarto(quartoId);
        boolean disponivel = disponibilidadeService.estaDisponivel(quartoId, entrada, saida);
        return new DisponibilidadeResponse(quartoId, entrada, saida, disponivel);
    }

    private Quarto buscarQuarto(Long id) {
        return quartoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quarto", id));
    }

    private Residencia buscarResidencia(Long id) {
        return residenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Residencia", id));
    }

    private QuartoResponse toResponse(Quarto quarto) {
        Long residenciaId = quarto.getResidencia() != null ? quarto.getResidencia().getId() : null;
        return new QuartoResponse(
                quarto.getId(),
                residenciaId,
                quarto.getNumero(),
                quarto.getTipo(),
                quarto.getValorBase(),
                quarto.isTemArCondicionado(),
                quarto.isTemHidromassagem(),
                quarto.getDescricao(),
                calculadoraDiaria.calcularValorDiaria(quarto));
    }
}
