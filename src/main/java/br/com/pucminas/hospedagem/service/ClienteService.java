package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.auth.LoginRequest;
import br.com.pucminas.hospedagem.dto.cliente.ClienteRequest;
import br.com.pucminas.hospedagem.dto.cliente.ClienteResponse;
import br.com.pucminas.hospedagem.dto.cliente.ClienteUpdateRequest;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.exception.ResourceNotFoundException;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servico de aplicacao responsavel pelo cadastro, consulta, atualizacao,
 * remocao e autenticacao de {@link Cliente}.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra um novo cliente, garantindo unicidade de email e CPF e
     * armazenando a senha em formato hash.
     *
     * @param request dados do cliente
     * @return representacao do cliente cadastrado (sem senha)
     * @throws BusinessException se ja existir cliente com o mesmo email ou CPF
     */
    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ja existe um cliente cadastrado com o email informado");
        }
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Ja existe um cliente cadastrado com o CPF informado");
        }

        Cliente cliente = new Cliente(
                request.nome(),
                request.cpf(),
                request.endereco(),
                request.telefone(),
                request.email(),
                passwordEncoder.encode(request.senha())
        );

        Cliente salvo = clienteRepository.save(cliente);
        return ClienteResponse.fromEntity(salvo);
    }

    /**
     * Autentica um cliente a partir de email e senha.
     *
     * @param request credenciais de acesso
     * @return o cliente autenticado
     * @throws BusinessException se o email nao existir ou a senha for invalida
     */
    @Transactional(readOnly = true)
    public Cliente autenticar(LoginRequest request) {
        Cliente cliente = clienteRepository.findByEmail(request.email())
                .filter(c -> passwordEncoder.matches(request.senha(), c.getSenha()))
                .orElseThrow(() -> new BusinessException("Email ou senha invalidos"));
        return cliente;
    }

    /**
     * Lista todos os clientes cadastrados.
     *
     * @return lista de representacoes (sem senha)
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    /**
     * Busca um cliente por identificador.
     *
     * @param id identificador do cliente
     * @return representacao do cliente (sem senha)
     * @throws ResourceNotFoundException se nao existir cliente com o id informado
     */
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return ClienteResponse.fromEntity(buscarEntidade(id));
    }

    /**
     * Atualiza os dados de um cliente existente. CPF e senha nao sao alterados.
     *
     * @param id      identificador do cliente
     * @param request novos dados
     * @return representacao atualizada (sem senha)
     * @throws ResourceNotFoundException se nao existir cliente com o id informado
     * @throws BusinessException         se o novo email ja pertencer a outro cliente
     */
    @Transactional
    public ClienteResponse atualizar(Long id, ClienteUpdateRequest request) {
        Cliente cliente = buscarEntidade(id);

        if (request.nome() != null) {
            cliente.setNome(request.nome());
        }
        if (request.endereco() != null) {
            cliente.setEndereco(request.endereco());
        }
        if (request.telefone() != null) {
            cliente.setTelefone(request.telefone());
        }
        if (request.email() != null && !request.email().equals(cliente.getEmail())) {
            if (clienteRepository.existsByEmail(request.email())) {
                throw new BusinessException("Ja existe um cliente cadastrado com o email informado");
            }
            cliente.setEmail(request.email());
        }

        Cliente atualizado = clienteRepository.save(cliente);
        return ClienteResponse.fromEntity(atualizado);
    }

    /**
     * Remove um cliente.
     *
     * @param id identificador do cliente
     * @throws ResourceNotFoundException se nao existir cliente com o id informado
     */
    @Transactional
    public void remover(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        clienteRepository.deleteById(id);
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
