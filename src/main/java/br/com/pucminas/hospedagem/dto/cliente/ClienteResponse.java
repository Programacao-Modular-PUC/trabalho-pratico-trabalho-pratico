package br.com.pucminas.hospedagem.dto.cliente;

import br.com.pucminas.hospedagem.model.Cliente;

/**
 * Representacao de saida de um {@code Cliente}.
 *
 * <p>Deliberadamente NAO expoe o campo senha.</p>
 */
public record ClienteResponse(
        Long id,
        String nome,
        String cpf,
        String endereco,
        String telefone,
        String email
) {

    /**
     * Cria um {@link ClienteResponse} a partir da entidade, sem expor a senha.
     *
     * @param cliente entidade de origem
     * @return DTO de resposta
     */
    public static ClienteResponse fromEntity(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEndereco(),
                cliente.getTelefone(),
                cliente.getEmail()
        );
    }
}
