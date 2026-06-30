package br.com.pucminas.hospedagem.dto.pagamento;

import br.com.pucminas.hospedagem.model.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisicao para quitar um pagamento.
 */
public record PagarRequest(
        @NotNull FormaPagamento formaPagamento
) {
}
