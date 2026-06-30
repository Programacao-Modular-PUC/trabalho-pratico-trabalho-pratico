package br.com.pucminas.hospedagem.service;

import br.com.pucminas.hospedagem.dto.auth.LoginRequest;
import br.com.pucminas.hospedagem.dto.cliente.ClienteRequest;
import br.com.pucminas.hospedagem.dto.cliente.ClienteResponse;
import br.com.pucminas.hospedagem.exception.BusinessException;
import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios de {@link ClienteService} usando Mockito para os repositorios.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequest novoClienteRequest() {
        return new ClienteRequest(
                "Joao da Silva",
                "529.982.247-25",
                "Rua A, 100",
                "31999990000",
                "joao@example.com",
                "senha123"
        );
    }

    @Test
    void cadastrarComSucessoCodificaSenhaERetornaResponseSemSenha() {
        ClienteRequest request = novoClienteRequest();

        when(clienteRepository.existsByEmail(request.email())).thenReturn(false);
        when(clienteRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-da-senha");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClienteResponse response = clienteService.cadastrar(request);

        // A response nao deve expor a senha (record sem campo senha).
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Joao da Silva");
        assertThat(response.cpf()).isEqualTo("529.982.247-25");
        assertThat(response.email()).isEqualTo("joao@example.com");

        // A senha persistida deve ser o hash gerado pelo encoder, nunca o texto puro.
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("hash-da-senha");
        assertThat(captor.getValue().getSenha()).isNotEqualTo("senha123");
    }

    @Test
    void cadastrarComEmailDuplicadoLancaBusinessException() {
        ClienteRequest request = novoClienteRequest();

        when(clienteRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(clienteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void autenticarComSucessoRetornaCliente() {
        LoginRequest login = new LoginRequest("joao@example.com", "senha123");
        Cliente cliente = new Cliente("Joao da Silva", "529.982.247-25",
                "Rua A, 100", "31999990000", "joao@example.com", "hash-da-senha");
        cliente.setId(7L);

        when(clienteRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("senha123", "hash-da-senha")).thenReturn(true);

        Cliente autenticado = clienteService.autenticar(login);

        assertThat(autenticado).isSameAs(cliente);
        assertThat(autenticado.getId()).isEqualTo(7L);
    }

    @Test
    void autenticarComSenhaInvalidaLancaBusinessException() {
        LoginRequest login = new LoginRequest("joao@example.com", "senha-errada");
        Cliente cliente = new Cliente("Joao da Silva", "529.982.247-25",
                "Rua A, 100", "31999990000", "joao@example.com", "hash-da-senha");
        cliente.setId(7L);

        when(clienteRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("senha-errada", "hash-da-senha")).thenReturn(false);

        assertThatThrownBy(() -> clienteService.autenticar(login))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email ou senha invalidos");
    }
}
