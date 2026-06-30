package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.reserva.ReservaRequest;
import br.com.pucminas.hospedagem.dto.reserva.ReservaResponse;
import br.com.pucminas.hospedagem.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST para gerenciamento de reservas futuras (requisito 4).
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * Cria uma nova reserva.
     */
    @PostMapping
    public ResponseEntity<ReservaResponse> criar(@Valid @RequestBody ReservaRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        ReservaResponse response = reservaService.criar(request);
        URI location = uriBuilder.path("/api/reservas/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Lista todas as reservas.
     */
    @GetMapping
    public ResponseEntity<Page<ReservaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(reservaService.listar(pageable));
    }

    /**
     * Busca uma reserva pelo id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    /**
     * Lista as reservas de um cliente.
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ReservaResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(reservaService.listarPorCliente(clienteId));
    }

    /**
     * Confirma uma reserva pendente.
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.confirmar(id));
    }

    /**
     * Cancela uma reserva.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }
}
