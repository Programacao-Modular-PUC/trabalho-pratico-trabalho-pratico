package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.residencia.ResidenciaRequest;
import br.com.pucminas.hospedagem.dto.residencia.ResidenciaResponse;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servico de aplicacao responsavel pelo CRUD de {@link Residencia}.
 */
@Service
public class ResidenciaService {

    private static final String RECURSO = "Residencia";

    private final ResidenciaRepository residenciaRepository;

    public ResidenciaService(ResidenciaRepository residenciaRepository) {
        this.residenciaRepository = residenciaRepository;
    }

    /** Cria uma nova residencia a partir dos dados informados. */
    @Transactional
    public ResidenciaResponse criar(ResidenciaRequest request) {
        Residencia residencia = new Residencia(
                request.endereco(),
                request.numero(),
                request.bairro(),
                request.cep(),
                request.telefone(),
                request.email()
        );
        Residencia salva = residenciaRepository.save(residencia);
        return ResidenciaResponse.fromEntity(salva);
    }

    /** Lista todas as residencias cadastradas. */
    @Transactional(readOnly = true)
    public List<ResidenciaResponse> listar() {
        return residenciaRepository.findAll()
                .stream()
                .map(ResidenciaResponse::fromEntity)
                .toList();
    }

    /** Busca uma residencia pelo id, lancando 404 caso nao exista. */
    @Transactional(readOnly = true)
    public ResidenciaResponse buscarPorId(Long id) {
        Residencia residencia = obterEntidade(id);
        return ResidenciaResponse.fromEntity(residencia);
    }

    /** Atualiza os dados de uma residencia existente, lancando 404 caso nao exista. */
    @Transactional
    public ResidenciaResponse atualizar(Long id, ResidenciaRequest request) {
        Residencia residencia = obterEntidade(id);
        residencia.setEndereco(request.endereco());
        residencia.setNumero(request.numero());
        residencia.setBairro(request.bairro());
        residencia.setCep(request.cep());
        residencia.setTelefone(request.telefone());
        residencia.setEmail(request.email());
        Residencia atualizada = residenciaRepository.save(residencia);
        return ResidenciaResponse.fromEntity(atualizada);
    }

    /** Remove uma residencia existente, lancando 404 caso nao exista. */
    @Transactional
    public void remover(Long id) {
        Residencia residencia = obterEntidade(id);
        residenciaRepository.delete(residencia);
    }

    private Residencia obterEntidade(Long id) {
        return residenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO, id));
    }
}
