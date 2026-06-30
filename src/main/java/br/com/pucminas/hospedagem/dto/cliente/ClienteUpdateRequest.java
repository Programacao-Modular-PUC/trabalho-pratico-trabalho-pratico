package br.com.pucminas.hospedagem.dto.cliente;

import jakarta.validation.constraints.Email;

/**
 * Dados de entrada para atualizacao de um {@code Cliente} existente.
 *
 * <p>Nao permite alterar CPF e nao exige nova senha. Os campos informados
 * substituem os valores atuais.</p>
 */
public record ClienteUpdateRequest(

        String nome,

        String endereco,

        String telefone,

        @Email(message = "Email invalido")
        String email
) {
}
