package br.com.pucminas.hospedagem.dto.residencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados de entrada para criacao/atualizacao de uma residencia.
 */
public record ResidenciaRequest(

        @NotBlank(message = "O endereco e obrigatorio.")
        String endereco,

        @NotBlank(message = "O numero e obrigatorio.")
        String numero,

        @NotBlank(message = "O bairro e obrigatorio.")
        String bairro,

        @NotBlank(message = "O cep e obrigatorio.")
        String cep,

        @NotBlank(message = "O telefone e obrigatorio.")
        String telefone,

        @NotBlank(message = "O email e obrigatorio.")
        @Email(message = "O email deve ser valido.")
        String email
) {
}
