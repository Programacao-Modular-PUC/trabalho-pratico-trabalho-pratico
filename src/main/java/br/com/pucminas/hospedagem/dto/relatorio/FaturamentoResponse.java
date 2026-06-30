package br.com.pucminas.hospedagem.dto.relatorio;

import java.math.BigDecimal;

/**
 * Relatorio de faturamento de uma residencia: quantidade de alugueis
 * realizados e valor total arrecadado.
 */
public record FaturamentoResponse(
        Long residenciaId,
        String residenciaEndereco,
        long quantidadeAlugueis,
        BigDecimal valorTotal) {
}
