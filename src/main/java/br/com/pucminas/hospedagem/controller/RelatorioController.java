package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.relatorio.FaturamentoResponse;
import br.com.pucminas.hospedagem.dto.relatorio.QuartoDisponivelResponse;
import br.com.pucminas.hospedagem.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints REST de relatorios gerenciais (faturamento e disponibilidade).
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    /** Faturamento total de uma residencia. */
    @GetMapping("/faturamento/residencia/{residenciaId}")
    public ResponseEntity<FaturamentoResponse> faturamentoPorResidencia(@PathVariable Long residenciaId) {
        return ResponseEntity.ok(relatorioService.faturamentoPorResidencia(residenciaId));
    }

    /** Quartos disponiveis em um periodo (opcionalmente filtrando por residencia). */
    @GetMapping("/quartos-disponiveis")
    public ResponseEntity<List<QuartoDisponivelResponse>> quartosDisponiveis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataSaida,
            @RequestParam(required = false) Long residenciaId) {
        return ResponseEntity.ok(
                relatorioService.quartosDisponiveis(dataEntrada, dataSaida, residenciaId));
    }
}
