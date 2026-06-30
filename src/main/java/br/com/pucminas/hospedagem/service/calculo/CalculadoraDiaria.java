package br.com.pucminas.hospedagem.service.calculo;

import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.model.Quarto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Responsavel pelo calculo do numero de diarias e dos valores de um aluguel.
 *
 * <p>Recebe, por injecao de dependencia, todas as estrategias de adicional
 * ({@link AdicionalDiaria}) disponiveis - aplicando o padrao <b>Strategy</b>.
 * Assim, o valor da diaria nunca e informado diretamente: e calculado a partir
 * do valor base do quarto somado aos adicionais aplicaveis (regra de negocio 2
 * e requisito 6).</p>
 *
 * <p><b>Regra das diarias (regra de negocio 1):</b> as diarias iniciam as 12h.
 * <ul>
 *   <li>Conta-se a diferenca em dias entre a data de entrada e a de saida;</li>
 *   <li>Saida apos as 12h adiciona uma nova diaria;</li>
 *   <li>Entrada apos as 12h conta como diaria completa (minimo de 1 diaria).</li>
 * </ul>
 * </p>
 */
@Service
public class CalculadoraDiaria {

    private static final LocalTime MEIO_DIA = LocalTime.NOON;

    private final List<AdicionalDiaria> adicionais;

    public CalculadoraDiaria(List<AdicionalDiaria> adicionais) {
        this.adicionais = adicionais;
    }

    /**
     * Calcula o valor de uma diaria do quarto: valor base + adicionais aplicaveis.
     */
    public BigDecimal calcularValorDiaria(Quarto quarto) {
        if (quarto.getValorBase() == null) {
            throw new BusinessException("O quarto deve possuir um valor base de diaria.");
        }
        BigDecimal total = quarto.getValorBase();
        for (AdicionalDiaria adicional : adicionais) {
            if (adicional.aplicaA(quarto)) {
                total = total.add(adicional.calcular(quarto));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a quantidade de diarias entre a entrada e a saida, segundo a
     * regra das 12h.
     */
    public long calcularNumeroDiarias(LocalDateTime entrada, LocalDateTime saida) {
        validarPeriodo(entrada, saida);
        long dias = ChronoUnit.DAYS.between(entrada.toLocalDate(), saida.toLocalDate());
        if (saida.toLocalTime().isAfter(MEIO_DIA)) {
            dias += 1;
        }
        if (dias < 1) {
            dias = 1;
        }
        return dias;
    }

    /**
     * Calcula o valor final do aluguel: valor da diaria x numero de diarias.
     */
    public BigDecimal calcularValorTotal(Quarto quarto, LocalDateTime entrada, LocalDateTime saida) {
        long diarias = calcularNumeroDiarias(entrada, saida);
        return calcularValorDiaria(quarto)
                .multiply(BigDecimal.valueOf(diarias))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validarPeriodo(LocalDateTime entrada, LocalDateTime saida) {
        if (entrada == null || saida == null) {
            throw new BusinessException("As datas de entrada e saida sao obrigatorias.");
        }
        if (!saida.isAfter(entrada)) {
            throw new BusinessException("A data de saida deve ser posterior a data de entrada.");
        }
    }
}
