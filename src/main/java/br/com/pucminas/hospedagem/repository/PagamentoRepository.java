package br.com.pucminas.hospedagem.repository;

import br.com.pucminas.hospedagem.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de {@link Pagamento}.
 */
@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByAluguelId(Long aluguelId);
}
