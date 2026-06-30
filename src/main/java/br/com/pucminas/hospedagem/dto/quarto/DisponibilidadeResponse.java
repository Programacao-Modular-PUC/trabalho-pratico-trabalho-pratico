package br.com.pucminas.hospedagem.dto.quarto;

import java.time.LocalDateTime;

/**
 * Resultado da verificacao de disponibilidade de um quarto em um periodo.
 */
public record DisponibilidadeResponse(
        Long quartoId,
        LocalDateTime dataEntrada,
        LocalDateTime dataSaida,
        boolean disponivel
) {
}
