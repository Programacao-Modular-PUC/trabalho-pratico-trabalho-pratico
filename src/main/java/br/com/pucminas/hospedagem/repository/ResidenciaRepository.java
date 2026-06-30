package br.com.pucminas.hospedagem.repository;

import br.com.pucminas.hospedagem.model.Residencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de {@link Residencia} (padrao Repository via Spring Data JPA).
 */
@Repository
public interface ResidenciaRepository extends JpaRepository<Residencia, Long> {
}
