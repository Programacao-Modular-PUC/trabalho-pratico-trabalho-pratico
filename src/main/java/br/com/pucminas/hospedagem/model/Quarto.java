package br.com.pucminas.hospedagem.model;

import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Quarto disponivel para aluguel em uma {@link Residencia}.
 *
 * <p>Pode ser individual ou para casal. Possui o valor base da diaria
 * (definido pelo proprietario) e os indicadores de itens adicionais
 * (ar-condicionado e banheira de hidromassagem), que influenciam o
 * calculo do valor final da diaria (requisitos 2 e 6).</p>
 */
@Entity
@Table(name = "quarto")
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificacao/numero do quarto dentro da residencia. */
    @Column(nullable = false)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoQuarto tipo;

    /** Valor base da diaria, definido pelo proprietario. */
    @Column(name = "valor_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBase;

    @Column(name = "tem_ar_condicionado", nullable = false)
    private boolean temArCondicionado;

    @Column(name = "tem_hidromassagem", nullable = false)
    private boolean temHidromassagem;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "residencia_id")
    private Residencia residencia;

    public Quarto() {
    }

    public Quarto(String numero, TipoQuarto tipo, BigDecimal valorBase,
                  boolean temArCondicionado, boolean temHidromassagem) {
        this.numero = numero;
        this.tipo = tipo;
        this.valorBase = valorBase;
        this.temArCondicionado = temArCondicionado;
        this.temHidromassagem = temHidromassagem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public TipoQuarto getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuarto tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    public boolean isTemArCondicionado() {
        return temArCondicionado;
    }

    public void setTemArCondicionado(boolean temArCondicionado) {
        this.temArCondicionado = temArCondicionado;
    }

    public boolean isTemHidromassagem() {
        return temHidromassagem;
    }

    public void setTemHidromassagem(boolean temHidromassagem) {
        this.temHidromassagem = temHidromassagem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public void setResidencia(Residencia residencia) {
        this.residencia = residencia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quarto)) {
            return false;
        }
        Quarto quarto = (Quarto) o;
        return id != null && id.equals(quarto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
