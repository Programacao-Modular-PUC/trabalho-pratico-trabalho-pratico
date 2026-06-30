package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.aluguel.AluguelRequest;
import br.com.pucminas.hospedagem.dto.aluguel.AluguelResponse;
import br.com.pucminas.hospedagem.dto.aluguel.ReciboResponse;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Aluguel;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.model.Pagamento;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Reserva;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.model.enums.StatusReserva;
import br.com.pucminas.hospedagem.recibo.Recibo;
import br.com.pucminas.hospedagem.repository.AluguelRepository;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import br.com.pucminas.hospedagem.repository.QuartoRepository;
import br.com.pucminas.hospedagem.repository.ReservaRepository;
import br.com.pucminas.hospedagem.service.calculo.CalculadoraDiaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servico responsavel pela realizacao e consulta de alugueis (hospedagens),
 * que constituem o nucleo do sistema.
 *
 * <p>Ao realizar um aluguel, valida a disponibilidade do quarto (regra de
 * negocio 3), calcula o numero de diarias e os valores (regra de negocio 1 e 2),
 * cria automaticamente o {@link Pagamento} pendente (regra de negocio 5) e,
 * quando o aluguel se origina de uma reserva, conclui a reserva.</p>
 */
@Service
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final QuartoRepository quartoRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final DisponibilidadeService disponibilidadeService;
    private final CalculadoraDiaria calculadoraDiaria;

    public AluguelService(AluguelRepository aluguelRepository,
                          QuartoRepository quartoRepository,
                          ClienteRepository clienteRepository,
                          ReservaRepository reservaRepository,
                          DisponibilidadeService disponibilidadeService,
                          CalculadoraDiaria calculadoraDiaria) {
        this.aluguelRepository = aluguelRepository;
        this.quartoRepository = quartoRepository;
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
        this.disponibilidadeService = disponibilidadeService;
        this.calculadoraDiaria = calculadoraDiaria;
    }

    /**
     * Realiza um aluguel: valida disponibilidade, calcula valores, cria o
     * pagamento pendente e, se houver, conclui a reserva de origem.
     */
    @Transactional
    public AluguelResponse realizar(AluguelRequest req) {
        Quarto quarto = quartoRepository.findById(req.quartoId())
                .orElseThrow(() -> new ResourceNotFoundException("Quarto", req.quartoId()));
        Cliente cliente = clienteRepository.findById(req.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", req.clienteId()));

        Residencia residencia = quarto.getResidencia();
        if (residencia == null) {
            throw new BusinessException(
                    "O quarto " + req.quartoId() + " nao esta vinculado a uma residencia.");
        }

        if (req.dataEntrada() != null && req.dataEntrada().isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data de entrada nao pode estar no passado.");
        }

        disponibilidadeService.validarDisponibilidade(
                req.quartoId(), req.dataEntrada(), req.dataSaida(), req.reservaId());

        long numeroDiarias = calculadoraDiaria.calcularNumeroDiarias(
                req.dataEntrada(), req.dataSaida());
        BigDecimal valorDiaria = calculadoraDiaria.calcularValorDiaria(quarto);
        BigDecimal valorFinal = calculadoraDiaria.calcularValorTotal(
                quarto, req.dataEntrada(), req.dataSaida());

        Aluguel aluguel = new Aluguel();
        aluguel.setResidencia(residencia);
        aluguel.setQuarto(quarto);
        aluguel.setCliente(cliente);
        aluguel.setDataEntrada(req.dataEntrada());
        aluguel.setDataSaida(req.dataSaida());
        aluguel.setNumeroDiarias(numeroDiarias);
        aluguel.setValorDiaria(valorDiaria);
        aluguel.setValorFinal(valorFinal);

        Pagamento pagamento = new Pagamento(aluguel, valorFinal);
        aluguel.setPagamento(pagamento);

        if (req.reservaId() != null) {
            Reserva reserva = reservaRepository.findById(req.reservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva", req.reservaId()));
            reserva.setStatus(StatusReserva.CONCLUIDA);
        }

        Aluguel salvo = aluguelRepository.save(aluguel);
        return AluguelResponse.de(salvo);
    }

    /** Lista todos os alugueis. */
    @Transactional(readOnly = true)
    public Page<AluguelResponse> listar(Pageable pageable) {
        return aluguelRepository.findAll(pageable).map(AluguelResponse::de);
    }

    /** Busca um aluguel pelo seu identificador. */
    @Transactional(readOnly = true)
    public AluguelResponse buscarPorId(Long id) {
        return AluguelResponse.de(buscarEntidade(id));
    }

    /** Lista o historico de alugueis de uma residencia (requisito 5). */
    @Transactional(readOnly = true)
    public List<AluguelResponse> listarPorResidencia(Long residenciaId) {
        return aluguelRepository.findByResidenciaId(residenciaId).stream()
                .map(AluguelResponse::de)
                .toList();
    }

    /** Lista o historico de alugueis de um cliente. */
    @Transactional(readOnly = true)
    public List<AluguelResponse> listarPorCliente(Long clienteId) {
        return aluguelRepository.findByClienteId(clienteId).stream()
                .map(AluguelResponse::de)
                .toList();
    }

    /** Exclui um aluguel (e, em cascata, o seu pagamento). */
    @Transactional
    public void excluir(Long id) {
        Aluguel aluguel = buscarEntidade(id);
        aluguelRepository.delete(aluguel);
    }

    /** Emite o recibo/formulario de aluguel (requisito 8). */
    @Transactional(readOnly = true)
    public ReciboResponse emitirRecibo(Long id) {
        Aluguel aluguel = buscarEntidade(id);
        Recibo recibo = Recibo.aPartirDe(aluguel);
        return ReciboResponse.de(recibo);
    }

    private Aluguel buscarEntidade(Long id) {
        return aluguelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluguel", id));
    }
}
