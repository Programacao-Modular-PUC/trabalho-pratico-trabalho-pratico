package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.pagamento.PagamentoResponse;
import br.com.pucminas.hospedagem.dto.pagamento.PagarRequest;
import br.com.pucminas.hospedagem.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST para a gestao de pagamentos.
 */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping
    public ResponseEntity<List<PagamentoResponse>> listar() {
        return ResponseEntity.ok(pagamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }

    @GetMapping("/aluguel/{aluguelId}")
    public ResponseEntity<PagamentoResponse> buscarPorAluguel(@PathVariable Long aluguelId) {
        return ResponseEntity.ok(pagamentoService.buscarPorAluguel(aluguelId));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<PagamentoResponse> pagar(@PathVariable Long id,
                                                   @Valid @RequestBody PagarRequest request) {
        return ResponseEntity.ok(pagamentoService.pagar(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PagamentoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.cancelar(id));
    }
}
