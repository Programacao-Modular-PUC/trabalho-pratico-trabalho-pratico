package br.com.pucminas.hospedagem.dto.quarto;

import br.com.pucminas.hospedagem.model.enums.TipoQuarto;

import java.math.BigDecimal;

/**
 * Dados de saida de um {@link br.com.pucminas.hospedagem.model.Quarto}.
 *
 * <p>Inclui o {@code valorDiariaCalculado}, ja somando os adicionais aplicaveis
 * (ar-condicionado e hidromassagem) ao valor base, conforme regra de negocio.</p>
 */
public record QuartoResponse(
        Long id,
        Long residenciaId,
        String numero,
        TipoQuarto tipo,
        BigDecimal valorBase,
        boolean temArCondicionado,
        boolean temHidromassagem,
        String descricao,
        BigDecimal valorDiariaCalculado
) {
}
