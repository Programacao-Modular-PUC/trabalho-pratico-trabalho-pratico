package br.com.pucminas.hospedagem.dto.relatorio;

import br.com.pucminas.hospedagem.model.enums.TipoQuarto;

import java.math.BigDecimal;

/**
 * Quarto disponivel em um periodo, com o valor da diaria ja calculado
 * (valor base + adicionais).
 */
public record QuartoDisponivelResponse(
        Long quartoId,
        Long residenciaId,
        String numero,
        TipoQuarto tipo,
        BigDecimal valorBase,
        BigDecimal valorDiaria) {
}
