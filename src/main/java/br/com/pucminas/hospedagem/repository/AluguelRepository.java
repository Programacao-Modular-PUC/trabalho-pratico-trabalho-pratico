package br.com.pucminas.hospedagem.repository;

import br.com.pucminas.hospedagem.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de {@link Aluguel}.
 */
@Repository
public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    /** Historico de alugueis de uma residencia (requisito 5). */
    List<Aluguel> findByResidenciaId(Long residenciaId);

    /** Historico de alugueis de um cliente. */
    List<Aluguel> findByClienteId(Long clienteId);

    /** Alugueis de um quarto. */
    List<Aluguel> findByQuartoId(Long quartoId);

    /**
     * Busca alugueis de um quarto que se sobrepoem ao periodo informado.
     * Dois periodos se sobrepoem quando: entrada_existente &lt; saida_nova
     * E saida_existente &gt; entrada_nova.
     */
    List<Aluguel> findByQuartoIdAndDataEntradaLessThanAndDataSaidaGreaterThan(
            Long quartoId, LocalDateTime novaSaida, LocalDateTime novaEntrada);
}
