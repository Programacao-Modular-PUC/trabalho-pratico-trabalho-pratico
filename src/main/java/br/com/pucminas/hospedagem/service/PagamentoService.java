package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.pagamento.PagamentoResponse;
import br.com.pucminas.hospedagem.dto.pagamento.PagarRequest;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Pagamento;
import br.com.pucminas.hospedagem.model.enums.StatusPagamento;
import br.com.pucminas.hospedagem.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servico de aplicacao para a gestao de {@link Pagamento}.
 */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listar() {
        return pagamentoRepository.findAll().stream()
                .map(PagamentoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorId(Long id) {
        return PagamentoResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorAluguel(Long aluguelId) {
        Pagamento pagamento = pagamentoRepository.findByAluguelId(aluguelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pagamento nao encontrado para o aluguel com id " + aluguelId));
        return PagamentoResponse.de(pagamento);
    }

    @Transactional
    public PagamentoResponse pagar(Long id, PagarRequest request) {
        Pagamento pagamento = buscarEntidade(id);
        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new BusinessException(
                    "Apenas pagamentos pendentes podem ser quitados. Status atual: "
                            + pagamento.getStatus());
        }
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setFormaPagamento(request.formaPagamento());
        pagamento.setDataPagamento(LocalDateTime.now());
        return PagamentoResponse.de(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse cancelar(Long id) {
        Pagamento pagamento = buscarEntidade(id);
        pagamento.setStatus(StatusPagamento.CANCELADO);
        return PagamentoResponse.de(pagamentoRepository.save(pagamento));
    }

    private Pagamento buscarEntidade(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", id));
    }
}
