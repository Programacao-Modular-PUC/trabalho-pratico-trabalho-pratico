package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.reserva.ReservaRequest;
import br.com.pucminas.hospedagem.dto.reserva.ReservaResponse;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Reserva;
import br.com.pucminas.hospedagem.model.enums.StatusReserva;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servico de aplicacao responsavel pelas reservas futuras de quartos
 * (requisito 4).
 *
 * <p>Coordena a busca dos agregados envolvidos (quarto e cliente), a
 * validacao de disponibilidade do periodo e as transicoes de status da
 * reserva ao longo do seu ciclo de vida.</p>
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final QuartoRepository quartoRepository;
    private final ClienteRepository clienteRepository;
    private final DisponibilidadeService disponibilidadeService;

    public ReservaService(ReservaRepository reservaRepository,
                          QuartoRepository quartoRepository,
                          ClienteRepository clienteRepository,
                          DisponibilidadeService disponibilidadeService) {
        this.reservaRepository = reservaRepository;
        this.quartoRepository = quartoRepository;
        this.clienteRepository = clienteRepository;
        this.disponibilidadeService = disponibilidadeService;
    }

    /**
     * Cria uma nova reserva (status PENDENTE) para o quarto e cliente
     * informados, apos validar a disponibilidade do periodo.
     *
     * @throws ResourceNotFoundException     se o quarto ou o cliente nao existir.
     * @throws br.com.pucminas.hospedagem.exception.QuartoIndisponivelException
     *         se o quarto estiver ocupado no periodo solicitado.
     */
    @Transactional
    public ReservaResponse criar(ReservaRequest request) {
        Quarto quarto = quartoRepository.findById(request.quartoId())
                .orElseThrow(() -> new ResourceNotFoundException("Quarto", request.quartoId()));

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.clienteId()));

        if (request.dataEntrada() != null && request.dataEntrada().isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data de entrada da reserva nao pode estar no passado.");
        }

        disponibilidadeService.validarDisponibilidade(
                quarto.getId(), request.dataEntrada(), request.dataSaida());

        Reserva reserva = new Reserva(quarto, cliente, request.dataEntrada(), request.dataSaida());
        Reserva salva = reservaRepository.save(reserva);
        return ReservaResponse.fromEntity(salva);
    }

    /**
     * Lista todas as reservas cadastradas.
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> listar() {
        return reservaRepository.findAll().stream()
                .map(ReservaResponse::fromEntity)
                .toList();
    }

    /**
     * Busca uma reserva pelo seu id.
     *
     * @throws ResourceNotFoundException se a reserva nao existir.
     */
    @Transactional(readOnly = true)
    public ReservaResponse buscarPorId(Long id) {
        return ReservaResponse.fromEntity(buscarEntidade(id));
    }

    /**
     * Lista as reservas de um determinado cliente.
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorCliente(Long clienteId) {
        return reservaRepository.findByClienteId(clienteId).stream()
                .map(ReservaResponse::fromEntity)
                .toList();
    }

    /**
     * Confirma uma reserva pendente, passando seu status para CONFIRMADA.
     *
     * @throws ResourceNotFoundException se a reserva nao existir.
     * @throws BusinessException         se a reserva estiver cancelada.
     */
    @Transactional
    public ReservaResponse confirmar(Long id) {
        Reserva reserva = buscarEntidade(id);
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new BusinessException("Nao e possivel confirmar uma reserva cancelada.");
        }
        if (reserva.getStatus() == StatusReserva.CONCLUIDA) {
            throw new BusinessException("Nao e possivel confirmar uma reserva ja concluida.");
        }
        reserva.setStatus(StatusReserva.CONFIRMADA);
        return ReservaResponse.fromEntity(reservaRepository.save(reserva));
    }

    /**
     * Cancela uma reserva, passando seu status para CANCELADA.
     *
     * @throws ResourceNotFoundException se a reserva nao existir.
     * @throws BusinessException         se a reserva ja estiver concluida.
     */
    @Transactional
    public ReservaResponse cancelar(Long id) {
        Reserva reserva = buscarEntidade(id);
        if (reserva.getStatus() == StatusReserva.CONCLUIDA) {
            throw new BusinessException("Nao e possivel cancelar uma reserva ja concluida.");
        }
        reserva.setStatus(StatusReserva.CANCELADA);
        return ReservaResponse.fromEntity(reservaRepository.save(reserva));
    }

    private Reserva buscarEntidade(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }
}
