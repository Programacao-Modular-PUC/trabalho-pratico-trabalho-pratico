package br.com.pucminas.hospedagem.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para autenticacao de um cliente.
 */
public record LoginRequest(

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        String senha
) {
}
