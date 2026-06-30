package br.com.pucminas.hospedagem.service.calculo;

import br.com.pucminas.hospedagem.model.Quarto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adicional cobrado quando o quarto possui banheira de hidromassagem.
 * O valor e parametrizavel via {@code hospedagem.adicional.hidromassagem}.
 */
@Component
public class AdicionalHidromassagem implements AdicionalDiaria {

    private final BigDecimal valor;

    public AdicionalHidromassagem(
            @Value("${hospedagem.adicional.hidromassagem:50.00}") BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public boolean aplicaA(Quarto quarto) {
        return quarto.isTemHidromassagem();
    }

    @Override
    public BigDecimal calcular(Quarto quarto) {
        return valor;
    }

    @Override
    public String descricao() {
        return "Banheira de hidromassagem";
    }
}
