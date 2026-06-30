package br.com.pucminas.hospedagem.dto.aluguel;

import br.com.pucminas.hospedagem.recibo.Recibo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacao do recibo/formulario de aluguel para respostas da API
 * (requisito 8).
 */
public record ReciboResponse(
        String cliente,
        String residencia,
        String quarto,
        LocalDateTime dataEntrada,
        LocalDateTime dataSaida,
        long numeroDiarias,
        BigDecimal valorDiaria,
        BigDecimal totalPagar,
        String textoFormatado
) {

    /** Converte um {@link Recibo} da fundacao para a sua representacao de resposta. */
    public static ReciboResponse de(Recibo recibo) {
        return new ReciboResponse(
                recibo.getCliente(),
                recibo.getResidencia(),
                recibo.getQuarto(),
                recibo.getDataEntrada(),
                recibo.getDataSaida(),
                recibo.getNumeroDiarias(),
                recibo.getValorDiaria(),
                recibo.getTotalPagar(),
                recibo.formatar()
        );
    }
}
