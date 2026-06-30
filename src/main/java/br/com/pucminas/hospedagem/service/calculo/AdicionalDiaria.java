package br.com.pucminas.hospedagem.service.calculo;

import br.com.pucminas.hospedagem.model.Quarto;

import java.math.BigDecimal;

/**
 * Estrategia de calculo de um item adicional que incide sobre o valor da
 * diaria (padrao de projeto <b>Strategy</b>).
 *
 * <p>Cada item adicional (ar-condicionado, hidromassagem, ...) implementa
 * esta interface. Para incluir um novo adicional basta criar uma nova
 * implementacao anotada com {@code @Component}, sem alterar o calculo
 * existente (principio aberto/fechado - modularidade).</p>
 */
public interface AdicionalDiaria {

    /** Indica se este adicional se aplica ao quarto informado. */
    boolean aplicaA(Quarto quarto);

    /** Valor que este adicional acrescenta a diaria do quarto. */
    BigDecimal calcular(Quarto quarto);

    /** Descricao do adicional (para exibicao/recibo). */
    String descricao();
}
