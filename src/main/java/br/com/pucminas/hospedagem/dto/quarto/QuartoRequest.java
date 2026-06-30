package br.com.pucminas.hospedagem.dto.quarto;

import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Dados de entrada para criacao/atualizacao de um {@link br.com.pucminas.hospedagem.model.Quarto}.
 */
public record QuartoRequest(

        @NotNull(message = "A residencia e obrigatoria.")
        Long residenciaId,

        @NotBlank(message = "O numero do quarto e obrigatorio.")
        String numero,

        @NotNull(message = "O tipo do quarto e obrigatorio.")
        TipoQuarto tipo,

        @NotNull(message = "O valor base e obrigatorio.")
        @Positive(message = "O valor base deve ser positivo.")
        BigDecimal valorBase,

        boolean temArCondicionado,

        boolean temHidromassagem,

        String descricao
) {
}
