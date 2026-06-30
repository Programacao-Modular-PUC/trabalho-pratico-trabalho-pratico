package br.com.pucminas.hospedagem.repository;

import br.com.pucminas.hospedagem.model.Reserva;
import br.com.pucminas.hospedagem.model.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de {@link Reserva}.
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByQuartoId(Long quartoId);

    /**
     * Busca reservas de um quarto, em determinados status, que se sobrepoem
     * ao periodo informado. Usado para validar a disponibilidade do quarto.
     */
    @Query("SELECT r FROM Reserva r WHERE r.quarto.id = :quartoId "
            + "AND r.status IN :statuses "
            + "AND r.dataEntrada < :saida AND r.dataSaida > :entrada")
    List<Reserva> findReservasConflitantes(@Param("quartoId") Long quartoId,
                                           @Param("entrada") LocalDateTime entrada,
                                           @Param("saida") LocalDateTime saida,
                                           @Param("statuses") List<StatusReserva> statuses);
}
