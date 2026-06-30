package br.com.pucminas.hospedagem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aluguel (hospedagem) de um quarto por um cliente em um determinado periodo.
 *
 * <p>Armazena residencia, quarto, cliente, datas de entrada e saida, a
 * quantidade de diarias e o valor final calculado (requisito 4). Todo aluguel
 * possui um {@link Pagamento} associado (regra de negocio 5).</p>
 */
@Entity
@Table(name = "aluguel")
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "residencia_id", nullable = false)
    private Residencia residencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "data_saida", nullable = false)
    private LocalDateTime dataSaida;

    @Column(name = "numero_diarias", nullable = false)
    private long numeroDiarias;

    /** Valor de cada diaria (valor base do quarto + adicionais). */
    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    /** Valor final do aluguel (valor da diaria x numero de diarias). */
    @Column(name = "valor_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @OneToOne(mappedBy = "aluguel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pagamento pagamento;

    public Aluguel() {
    }

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    /** Associa um pagamento a este aluguel, mantendo a relacao bidirecional. */
    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
        if (pagamento != null) {
            pagamento.setAluguel(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public void setResidencia(Residencia residencia) {
        this.residencia = residencia;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public void setQuarto(Quarto quarto) {
        this.quarto = quarto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public long getNumeroDiarias() {
        return numeroDiarias;
    }

    public void setNumeroDiarias(long numeroDiarias) {
        this.numeroDiarias = numeroDiarias;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Aluguel)) {
            return false;
        }
        Aluguel aluguel = (Aluguel) o;
        return id != null && id.equals(aluguel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
