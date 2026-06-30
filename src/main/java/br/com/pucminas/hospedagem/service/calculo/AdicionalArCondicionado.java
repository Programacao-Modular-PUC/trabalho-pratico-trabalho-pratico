package br.com.pucminas.hospedagem.service.calculo;

import br.com.pucminas.hospedagem.model.Quarto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adicional cobrado quando o quarto possui ar-condicionado.
 * O valor e parametrizavel via {@code hospedagem.adicional.ar-condicionado}.
 */
@Component
public class AdicionalArCondicionado implements AdicionalDiaria {

    private final BigDecimal valor;

    public AdicionalArCondicionado(
            @Value("${hospedagem.adicional.ar-condicionado:30.00}") BigDecimal valor) {
        this.valor = valor;
    }

    @Override
    public boolean aplicaA(Quarto quarto) {
        return quarto.isTemArCondicionado();
    }

    @Override
    public BigDecimal calcular(Quarto quarto) {
        return valor;
    }

    @Override
    public String descricao() {
        return "Ar-condicionado";
    }
}
