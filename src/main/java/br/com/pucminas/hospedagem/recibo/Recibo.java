package br.com.pucminas.hospedagem.recibo;

import br.com.pucminas.hospedagem.model.Aluguel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Recibo/formulario de aluguel, construido via padrao de projeto <b>Builder</b>.
 *
 * <p>Atende ao requisito 8: impressao do formulario de aluguel com data e
 * horario de entrada, data e horario de saida, numero de diarias e total a
 * pagar.</p>
 */
public class Recibo {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String cliente;
    private final String residencia;
    private final String quarto;
    private final LocalDateTime dataEntrada;
    private final LocalDateTime dataSaida;
    private final long numeroDiarias;
    private final BigDecimal valorDiaria;
    private final BigDecimal totalPagar;

    private Recibo(Builder builder) {
        this.cliente = builder.cliente;
        this.residencia = builder.residencia;
        this.quarto = builder.quarto;
        this.dataEntrada = builder.dataEntrada;
        this.dataSaida = builder.dataSaida;
        this.numeroDiarias = builder.numeroDiarias;
        this.valorDiaria = builder.valorDiaria;
        this.totalPagar = builder.totalPagar;
    }

    /** Cria um recibo a partir de um aluguel ja calculado. */
    public static Recibo aPartirDe(Aluguel aluguel) {
        return new Builder()
                .cliente(aluguel.getCliente() != null ? aluguel.getCliente().getNome() : null)
                .residencia(formatarResidencia(aluguel))
                .quarto(aluguel.getQuarto() != null ? aluguel.getQuarto().getNumero() : null)
                .dataEntrada(aluguel.getDataEntrada())
                .dataSaida(aluguel.getDataSaida())
                .numeroDiarias(aluguel.getNumeroDiarias())
                .valorDiaria(aluguel.getValorDiaria())
                .totalPagar(aluguel.getValorFinal())
                .build();
    }

    private static String formatarResidencia(Aluguel aluguel) {
        if (aluguel.getResidencia() == null) {
            return null;
        }
        return aluguel.getResidencia().getEndereco() + ", " + aluguel.getResidencia().getNumero()
                + " - " + aluguel.getResidencia().getBairro();
    }

    /**
     * Gera o texto do formulario de aluguel no formato exigido pelo trabalho.
     */
    public String formatar() {
        StringBuilder sb = new StringBuilder();
        sb.append("=============== RECIBO DE HOSPEDAGEM ===============\n");
        if (cliente != null) {
            sb.append("Cliente: ").append(cliente).append('\n');
        }
        if (residencia != null) {
            sb.append("Residencia: ").append(residencia).append('\n');
        }
        if (quarto != null) {
            sb.append("Quarto: ").append(quarto).append('\n');
        }
        sb.append("Data e horario de entrada: ").append(formatar(dataEntrada)).append('\n');
        sb.append("Data e horario de saida: ").append(formatar(dataSaida)).append('\n');
        sb.append("Numero de diarias: ").append(numeroDiarias).append('\n');
        if (valorDiaria != null) {
            sb.append("Valor da diaria: R$ ").append(valorDiaria).append('\n');
        }
        sb.append("Total a pagar: R$ ").append(totalPagar).append('\n');
        sb.append("====================================================");
        return sb.toString();
    }

    private String formatar(LocalDateTime dataHora) {
        return dataHora != null ? dataHora.format(FORMATO_DATA_HORA) : "-";
    }

    public String getCliente() {
        return cliente;
    }

    public String getResidencia() {
        return residencia;
    }

    public String getQuarto() {
        return quarto;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public long getNumeroDiarias() {
        return numeroDiarias;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public BigDecimal getTotalPagar() {
        return totalPagar;
    }

    /**
     * Construtor fluente do {@link Recibo} (padrao Builder).
     */
    public static class Builder {
        private String cliente;
        private String residencia;
        private String quarto;
        private LocalDateTime dataEntrada;
        private LocalDateTime dataSaida;
        private long numeroDiarias;
        private BigDecimal valorDiaria;
        private BigDecimal totalPagar;

        public Builder cliente(String cliente) {
            this.cliente = cliente;
            return this;
        }

        public Builder residencia(String residencia) {
            this.residencia = residencia;
            return this;
        }

        public Builder quarto(String quarto) {
            this.quarto = quarto;
            return this;
        }

        public Builder dataEntrada(LocalDateTime dataEntrada) {
            this.dataEntrada = dataEntrada;
            return this;
        }

        public Builder dataSaida(LocalDateTime dataSaida) {
            this.dataSaida = dataSaida;
            return this;
        }

        public Builder numeroDiarias(long numeroDiarias) {
            this.numeroDiarias = numeroDiarias;
            return this;
        }

        public Builder valorDiaria(BigDecimal valorDiaria) {
            this.valorDiaria = valorDiaria;
            return this;
        }

        public Builder totalPagar(BigDecimal totalPagar) {
            this.totalPagar = totalPagar;
            return this;
        }

        public Recibo build() {
            return new Recibo(this);
        }
    }
}
