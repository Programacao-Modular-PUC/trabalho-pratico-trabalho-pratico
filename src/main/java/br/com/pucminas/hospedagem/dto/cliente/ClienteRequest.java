package br.com.pucminas.hospedagem.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

/**
 * Dados de entrada para cadastro de um novo {@code Cliente}.
 *
 * <p>A senha informada e armazenada apenas em formato hash; nunca e retornada
 * em respostas da API.</p>
 */
public record ClienteRequest(

        @NotBlank(message = "O nome e obrigatorio")
        String nome,

        @NotBlank(message = "O CPF e obrigatorio")
        @CPF(message = "CPF invalido")
        String cpf,

        @NotBlank(message = "O endereco e obrigatorio")
        String endereco,

        @NotBlank(message = "O telefone e obrigatorio")
        String telefone,

        @NotBlank(message = "O email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
        String senha
) {
}
