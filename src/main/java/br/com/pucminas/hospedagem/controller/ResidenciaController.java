package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.residencia.ResidenciaRequest;
import br.com.pucminas.hospedagem.dto.residencia.ResidenciaResponse;
import br.com.pucminas.hospedagem.service.ResidenciaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST para gerenciamento de residencias.
 */
@RestController
@RequestMapping("/api/residencias")
public class ResidenciaController {

    private final ResidenciaService residenciaService;

    public ResidenciaController(ResidenciaService residenciaService) {
        this.residenciaService = residenciaService;
    }

    @PostMapping
    public ResponseEntity<ResidenciaResponse> criar(@Valid @RequestBody ResidenciaRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        ResidenciaResponse response = residenciaService.criar(request);
        URI location = uriBuilder.path("/api/residencias/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ResidenciaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(residenciaService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidenciaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(residenciaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResidenciaResponse> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ResidenciaRequest request) {
        return ResponseEntity.ok(residenciaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        residenciaService.remover(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
