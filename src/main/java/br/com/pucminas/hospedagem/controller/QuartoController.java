package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.quarto.DisponibilidadeResponse;
import br.com.pucminas.hospedagem.dto.quarto.QuartoRequest;
import br.com.pucminas.hospedagem.dto.quarto.QuartoResponse;
import br.com.pucminas.hospedagem.service.QuartoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API REST para gestao de quartos (requisitos 1 e 2).
 */
@RestController
@RequestMapping("/api/quartos")
public class QuartoController {

    private final QuartoService quartoService;

    public QuartoController(QuartoService quartoService) {
        this.quartoService = quartoService;
    }

    @PostMapping
    public ResponseEntity<QuartoResponse> criar(@Valid @RequestBody QuartoRequest request) {
        QuartoResponse response = quartoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<QuartoResponse>> listar(
            @RequestParam(required = false) Long residenciaId) {
        List<QuartoResponse> response = residenciaId != null
                ? quartoService.listarPorResidencia(residenciaId)
                : quartoService.listar();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuartoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(quartoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuartoResponse> atualizar(@PathVariable Long id,
                                                    @Valid @RequestBody QuartoRequest request) {
        return ResponseEntity.ok(quartoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        quartoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> verificarDisponibilidade(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataEntrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataSaida) {
        return ResponseEntity.ok(
                quartoService.verificarDisponibilidade(id, dataEntrada, dataSaida));
    }
}
