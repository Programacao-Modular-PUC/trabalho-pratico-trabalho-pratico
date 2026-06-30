package br.com.pucminas.hospedagem.dto.residencia;

import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;

import java.math.BigDecimal;

/**
 * Representacao minima de um quarto, usada nas respostas de residencia.
 */
public record QuartoResumoResponse(
        Long id,
        String numero,
        TipoQuarto tipo,
        BigDecimal valorBase
) {

    /** Converte uma entidade {@link Quarto} em sua representacao resumida. */
    public static QuartoResumoResponse fromEntity(Quarto quarto) {
        return new QuartoResumoResponse(
                quarto.getId(),
                quarto.getNumero(),
                quarto.getTipo(),
                quarto.getValorBase()
        );
    }
}
