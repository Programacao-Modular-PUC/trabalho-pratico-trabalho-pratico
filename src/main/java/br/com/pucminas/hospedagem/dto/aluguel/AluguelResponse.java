package br.com.pucminas.hospedagem.dto.aluguel;

import br.com.pucminas.hospedagem.model.Aluguel;
import br.com.pucminas.hospedagem.model.enums.StatusPagamento;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacao (achatada) de um aluguel para respostas da API.
 *
 * <p>Nao expoe entidades; apenas os campos relevantes, incluindo dados da
 * residencia, do quarto e do cliente associados.</p>
 */
public record AluguelResponse(
        Long id,
        Long residenciaId,
        String residenciaEndereco,
        Long quartoId,
        String quartoNumero,
        TipoQuarto tipoQuarto,
        Long clienteId,
        String clienteNome,
        LocalDateTime dataEntrada,
        LocalDateTime dataSaida,
        long numeroDiarias,
        BigDecimal valorDiaria,
        BigDecimal valorFinal,
        StatusPagamento statusPagamento,
        LocalDateTime dataCriacao
) {

    /** Converte uma entidade {@link Aluguel} para a sua representacao de resposta. */
    public static AluguelResponse de(Aluguel aluguel) {
        var residencia = aluguel.getResidencia();
        var quarto = aluguel.getQuarto();
        var cliente = aluguel.getCliente();
        var pagamento = aluguel.getPagamento();

        return new AluguelResponse(
                aluguel.getId(),
                residencia != null ? residencia.getId() : null,
                residencia != null ? residencia.getEndereco() : null,
                quarto != null ? quarto.getId() : null,
                quarto != null ? quarto.getNumero() : null,
                quarto != null ? quarto.getTipo() : null,
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                aluguel.getDataEntrada(),
                aluguel.getDataSaida(),
                aluguel.getNumeroDiarias(),
                aluguel.getValorDiaria(),
                aluguel.getValorFinal(),
                pagamento != null ? pagamento.getStatus() : null,
                aluguel.getDataCriacao()
        );
    }
}
