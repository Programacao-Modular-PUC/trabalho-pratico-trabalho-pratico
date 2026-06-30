package br.com.pucminas.hospedagem.dto.aluguel;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Dados de entrada para realizar um aluguel (hospedagem) de um quarto.
 *
 * @param quartoId    identificador do quarto a ser alugado (obrigatorio).
 * @param clienteId   identificador do cliente que realiza o aluguel (obrigatorio).
 * @param dataEntrada data e horario de entrada (obrigatorio).
 * @param dataSaida   data e horario de saida (obrigatorio).
 * @param reservaId   identificador de uma reserva a ser convertida em aluguel
 *                    (opcional, pode ser {@code null}).
 */
public record AluguelRequest(
        @NotNull(message = "O id do quarto e obrigatorio.")
        Long quartoId,

        @NotNull(message = "O id do cliente e obrigatorio.")
        Long clienteId,

        @NotNull(message = "A data de entrada e obrigatoria.")
        LocalDateTime dataEntrada,

        @NotNull(message = "A data de saida e obrigatoria.")
        LocalDateTime dataSaida,

        Long reservaId
) {
}
