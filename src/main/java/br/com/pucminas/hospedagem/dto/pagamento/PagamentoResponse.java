package br.com.pucminas.hospedagem.dto.pagamento;

import br.com.pucminas.hospedagem.model.Pagamento;
import br.com.pucminas.hospedagem.model.enums.FormaPagamento;
import br.com.pucminas.hospedagem.model.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacao de saida de um {@link Pagamento}.
 */
public record PagamentoResponse(
        Long id,
        Long aluguelId,
        BigDecimal valor,
        StatusPagamento status,
        FormaPagamento formaPagamento,
        LocalDateTime dataPagamento,
        LocalDateTime dataCriacao
) {

    /**
     * Converte uma entidade {@link Pagamento} no seu DTO de resposta.
     */
    public static PagamentoResponse de(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getAluguel() != null ? pagamento.getAluguel().getId() : null,
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getFormaPagamento(),
                pagamento.getDataPagamento(),
                pagamento.getDataCriacao()
        );
    }
}
