package br.com.pucminas.hospedagem.dto.reserva;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Dados de entrada para a criacao de uma reserva (requisito 4 - reservas futuras).
 *
 * @param quartoId    id do quarto a ser reservado.
 * @param clienteId   id do cliente que faz a reserva.
 * @param dataEntrada data/hora de inicio do periodo reservado.
 * @param dataSaida   data/hora de fim do periodo reservado.
 */
public record ReservaRequest(

        @NotNull(message = "O id do quarto e obrigatorio.")
        Long quartoId,

        @NotNull(message = "O id do cliente e obrigatorio.")
        Long clienteId,

        @NotNull(message = "A data de entrada e obrigatoria.")
        LocalDateTime dataEntrada,

        @NotNull(message = "A data de saida e obrigatoria.")
        LocalDateTime dataSaida
) {
}
