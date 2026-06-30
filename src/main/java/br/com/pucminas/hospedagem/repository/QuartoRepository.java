package br.com.pucminas.hospedagem.repository;

import br.com.pucminas.hospedagem.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de {@link Quarto}.
 */
@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    /** Lista os quartos de uma residencia. */
    List<Quarto> findByResidenciaId(Long residenciaId);
}
