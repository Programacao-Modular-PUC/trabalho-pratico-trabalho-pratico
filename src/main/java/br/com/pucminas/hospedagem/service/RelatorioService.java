package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.relatorio.FaturamentoResponse;
import br.com.pucminas.hospedagem.dto.relatorio.QuartoDisponivelResponse;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Aluguel;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.repository.AluguelRepository;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import br.com.pucminas.hospedagem.service.calculo.CalculadoraDiaria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Servico de relatorios gerenciais do sistema de hospedagem.
 *
 * <p>Fornece consultas analiticas que combinam dados de varios agregados,
 * como o faturamento por residencia e os quartos disponiveis em um periodo.</p>
 */
@Service
public class RelatorioService {

    private final AluguelRepository aluguelRepository;
    private final ResidenciaRepository residenciaRepository;
    private final QuartoRepository quartoRepository;
    private final DisponibilidadeService disponibilidadeService;
    private final CalculadoraDiaria calculadoraDiaria;

    public RelatorioService(AluguelRepository aluguelRepository,
                            ResidenciaRepository residenciaRepository,
                            QuartoRepository quartoRepository,
                            DisponibilidadeService disponibilidadeService,
                            CalculadoraDiaria calculadoraDiaria) {
        this.aluguelRepository = aluguelRepository;
        this.residenciaRepository = residenciaRepository;
        this.quartoRepository = quartoRepository;
        this.disponibilidadeService = disponibilidadeService;
        this.calculadoraDiaria = calculadoraDiaria;
    }

    /**
     * Calcula o faturamento de uma residencia (soma dos valores finais de
     * todos os seus alugueis).
     *
     * @throws ResourceNotFoundException se a residencia nao existir.
     */
    @Transactional(readOnly = true)
    public FaturamentoResponse faturamentoPorResidencia(Long residenciaId) {
        Residencia residencia = residenciaRepository.findById(residenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Residencia", residenciaId));

        List<Aluguel> alugueis = aluguelRepository.findByResidenciaId(residenciaId);
        BigDecimal valorTotal = alugueis.stream()
                .map(Aluguel::getValorFinal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FaturamentoResponse(
                residencia.getId(), residencia.getEndereco(), alugueis.size(), valorTotal);
    }

    /**
     * Lista os quartos disponiveis em um periodo, opcionalmente filtrando por
     * residencia. O valor da diaria (base + adicionais) ja vem calculado.
     */
    @Transactional(readOnly = true)
    public List<QuartoDisponivelResponse> quartosDisponiveis(LocalDateTime entrada,
                                                             LocalDateTime saida,
                                                             Long residenciaId) {
        List<Quarto> quartos = (residenciaId != null)
                ? quartoRepository.findByResidenciaId(residenciaId)
                : quartoRepository.findAll();

        return quartos.stream()
                .filter(quarto -> disponibilidadeService.estaDisponivel(quarto.getId(), entrada, saida))
                .map(this::toDisponivelResponse)
                .toList();
    }

    private QuartoDisponivelResponse toDisponivelResponse(Quarto quarto) {
        Long residenciaId = quarto.getResidencia() != null ? quarto.getResidencia().getId() : null;
        return new QuartoDisponivelResponse(
                quarto.getId(),
                residenciaId,
                quarto.getNumero(),
                quarto.getTipo(),
                quarto.getValorBase(),
                calculadoraDiaria.calcularValorDiaria(quarto));
    }
}
