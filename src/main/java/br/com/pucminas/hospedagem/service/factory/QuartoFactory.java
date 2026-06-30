package br.com.pucminas.hospedagem.service.factory;

import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fabrica de {@link Quarto} (padrao de projeto <b>Factory</b>).
 *
 * <p>Centraliza a criacao de quartos, encapsulando a montagem do objeto a
 * partir do tipo (individual ou casal) e seus atributos.</p>
 */
@Component
public class QuartoFactory {

    public Quarto criar(String numero, TipoQuarto tipo, BigDecimal valorBase,
                        boolean temArCondicionado, boolean temHidromassagem, String descricao) {
        Quarto quarto = new Quarto(numero, tipo, valorBase, temArCondicionado, temHidromassagem);
        quarto.setDescricao(descricao);
        return quarto;
    }

    public Quarto criarIndividual(String numero, BigDecimal valorBase,
                                  boolean temArCondicionado, boolean temHidromassagem) {
        return criar(numero, TipoQuarto.INDIVIDUAL, valorBase, temArCondicionado, temHidromassagem, null);
    }

    public Quarto criarCasal(String numero, BigDecimal valorBase,
                             boolean temArCondicionado, boolean temHidromassagem) {
        return criar(numero, TipoQuarto.CASAL, valorBase, temArCondicionado, temHidromassagem, null);
    }
}
