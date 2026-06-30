package br.com.pucminas.hospedagem.model.enums;

/**
 * Situacao do pagamento associado a um aluguel.
 */
public enum StatusPagamento {
    /** Pagamento gerado, ainda nao quitado. */
    PENDENTE,
    /** Pagamento quitado. */
    PAGO,
    /** Pagamento cancelado. */
    CANCELADO
}
