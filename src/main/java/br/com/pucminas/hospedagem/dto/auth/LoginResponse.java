package br.com.pucminas.hospedagem.dto.auth;

/**
 * Resposta de uma autenticacao bem-sucedida.
 */
public record LoginResponse(
        String mensagem,
        Long clienteId,
        String nome,
        String email
) {
}
