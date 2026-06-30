package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.aluguel.AluguelRequest;
import br.com.pucminas.hospedagem.dto.aluguel.AluguelResponse;
import br.com.pucminas.hospedagem.dto.aluguel.ReciboResponse;
import br.com.pucminas.hospedagem.service.AluguelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST para realizacao e consulta de alugueis (hospedagens) e
 * emissao do recibo de aluguel.
 */
@RestController
@RequestMapping("/api/alugueis")
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(AluguelService aluguelService) {
        this.aluguelService = aluguelService;
    }

    /** Realiza um novo aluguel. */
    @PostMapping
    public ResponseEntity<AluguelResponse> realizar(@Valid @RequestBody AluguelRequest request) {
        AluguelResponse response = aluguelService.realizar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Lista todos os alugueis. */
    @GetMapping
    public ResponseEntity<List<AluguelResponse>> listar() {
        return ResponseEntity.ok(aluguelService.listar());
    }

    /** Busca um aluguel pelo seu identificador. */
    @GetMapping("/{id}")
    public ResponseEntity<AluguelResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(aluguelService.buscarPorId(id));
    }

    /** Emite o recibo/formulario de aluguel. */
    @GetMapping("/{id}/recibo")
    public ResponseEntity<ReciboResponse> emitirRecibo(@PathVariable Long id) {
        return ResponseEntity.ok(aluguelService.emitirRecibo(id));
    }

    /** Lista o historico de alugueis de uma residencia. */
    @GetMapping("/residencia/{residenciaId}")
    public ResponseEntity<List<AluguelResponse>> listarPorResidencia(
            @PathVariable Long residenciaId) {
        return ResponseEntity.ok(aluguelService.listarPorResidencia(residenciaId));
    }

    /** Lista o historico de alugueis de um cliente. */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AluguelResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(aluguelService.listarPorCliente(clienteId));
    }

    /** Exclui um aluguel. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        aluguelService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
