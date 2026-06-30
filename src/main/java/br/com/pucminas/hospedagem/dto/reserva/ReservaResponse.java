package br.com.pucminas.hospedagem.dto.reserva;

import br.com.pucminas.hospedagem.model.Reserva;
import br.com.pucminas.hospedagem.model.enums.StatusReserva;

import java.time.LocalDateTime;

/**
 * Representacao de uma reserva nas respostas da API.
 *
 * @param id           identificador da reserva.
 * @param quartoId     id do quarto reservado.
 * @param quartoNumero numero/identificacao do quarto reservado.
 * @param clienteId    id do cliente.
 * @param clienteNome  nome do cliente.
 * @param dataEntrada  inicio do periodo reservado.
 * @param dataSaida    fim do periodo reservado.
 * @param status       situacao atual da reserva.
 * @param dataCriacao  momento em que a reserva foi criada.
 */
public record ReservaResponse(
        Long id,
        Long quartoId,
        String quartoNumero,
        Long clienteId,
        String clienteNome,
        LocalDateTime dataEntrada,
        LocalDateTime dataSaida,
        StatusReserva status,
        LocalDateTime dataCriacao
) {

    /**
     * Cria um {@link ReservaResponse} a partir da entidade {@link Reserva}.
     */
    public static ReservaResponse fromEntity(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getQuarto() != null ? reserva.getQuarto().getId() : null,
                reserva.getQuarto() != null ? reserva.getQuarto().getNumero() : null,
                reserva.getCliente() != null ? reserva.getCliente().getId() : null,
                reserva.getCliente() != null ? reserva.getCliente().getNome() : null,
                reserva.getDataEntrada(),
                reserva.getDataSaida(),
                reserva.getStatus(),
                reserva.getDataCriacao()
        );
    }
}
