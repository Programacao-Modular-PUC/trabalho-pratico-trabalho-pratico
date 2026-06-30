package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.QuartoIndisponivelException;
import br.com.pucminas.hospedagem.model.enums.StatusReserva;
import br.com.pucminas.hospedagem.repository.AluguelRepository;
import br.com.pucminas.hospedagem.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controla a disponibilidade de um quarto em um determinado periodo
 * (regra de negocio 3: um quarto nao pode ser alugado se ja estiver ocupado).
 *
 * <p>Considera tanto os alugueis existentes quanto as reservas ativas
 * (PENDENTE ou CONFIRMADA) que se sobreponham ao periodo solicitado.</p>
 */
@Service
public class DisponibilidadeService {

    private static final List<StatusReserva> RESERVAS_ATIVAS =
            List.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

    private final AluguelRepository aluguelRepository;
    private final ReservaRepository reservaRepository;

    public DisponibilidadeService(AluguelRepository aluguelRepository,
                                  ReservaRepository reservaRepository) {
        this.aluguelRepository = aluguelRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Verifica se o quarto esta disponivel no periodo, desconsiderando uma
     * reserva especifica (util ao converter uma reserva em aluguel).
     *
     * @param reservaIdIgnorada id de reserva a ser ignorada na verificacao
     *                          (pode ser {@code null}).
     */
    public boolean estaDisponivel(Long quartoId, LocalDateTime entrada, LocalDateTime saida,
                                  Long reservaIdIgnorada) {
        validarPeriodo(entrada, saida);

        boolean possuiAluguelConflitante = !aluguelRepository
                .findByQuartoIdAndDataEntradaLessThanAndDataSaidaGreaterThan(quartoId, saida, entrada)
                .isEmpty();
        if (possuiAluguelConflitante) {
            return false;
        }

        return reservaRepository
                .findReservasConflitantes(quartoId, entrada, saida, RESERVAS_ATIVAS)
                .stream()
                .allMatch(reserva -> reserva.getId().equals(reservaIdIgnorada));
    }

    public boolean estaDisponivel(Long quartoId, LocalDateTime entrada, LocalDateTime saida) {
        return estaDisponivel(quartoId, entrada, saida, null);
    }

    /**
     * Garante que o quarto esteja disponivel, lancando
     * {@link QuartoIndisponivelException} caso contrario.
     */
    public void validarDisponibilidade(Long quartoId, LocalDateTime entrada, LocalDateTime saida,
                                       Long reservaIdIgnorada) {
        if (!estaDisponivel(quartoId, entrada, saida, reservaIdIgnorada)) {
            throw new QuartoIndisponivelException(quartoId, entrada, saida);
        }
    }

    public void validarDisponibilidade(Long quartoId, LocalDateTime entrada, LocalDateTime saida) {
        validarDisponibilidade(quartoId, entrada, saida, null);
    }

    private void validarPeriodo(LocalDateTime entrada, LocalDateTime saida) {
        if (entrada == null || saida == null) {
            throw new BusinessException("As datas de entrada e saida sao obrigatorias.");
        }
        if (!saida.isAfter(entrada)) {
            throw new BusinessException("A data de saida deve ser posterior a data de entrada.");
        }
    }
}
