package br.com.pucminas.hospedagem.controller;

import br.com.pucminas.hospedagem.dto.auth.LoginRequest;
import br.com.pucminas.hospedagem.dto.auth.LoginResponse;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para autenticacao de clientes.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClienteService clienteService;

    public AuthController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Cliente cliente = clienteService.autenticar(request);
        LoginResponse response = new LoginResponse(
                "Autenticacao realizada com sucesso",
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail()
        );
        return ResponseEntity.ok(response);
    }
}
