package br.com.pucminas.hospedagem.model.enums;

/**
 * Situacao de uma reserva ao longo do seu ciclo de vida.
 */
public enum StatusReserva {
    /** Reserva criada, aguardando confirmacao. */
    PENDENTE,
    /** Reserva confirmada; o quarto fica bloqueado no periodo. */
    CONFIRMADA,
    /** Reserva cancelada; o quarto volta a ficar disponivel. */
    CANCELADA,
    /** Reserva concluida (convertida em aluguel/hospedagem realizada). */
    CONCLUIDA
}
