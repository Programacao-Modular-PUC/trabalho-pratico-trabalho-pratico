package br.com.pucminas.hospedagem.config;

import br.com.pucminas.hospedagem.model.Cliente;
import br.com.pucminas.hospedagem.model.Quarto;
import br.com.pucminas.hospedagem.model.Residencia;
import br.com.pucminas.hospedagem.model.enums.TipoQuarto;
import br.com.pucminas.hospedagem.repository.ClienteRepository;
import br.com.pucminas.hospedagem.repository.ResidenciaRepository;
import br.com.pucminas.hospedagem.service.factory.QuartoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Carga opcional de dados de demonstracao.
 *
 * <p>So executa quando {@code hospedagem.seed.enabled=true}. Util para testar
 * rapidamente a API sem precisar cadastrar tudo manualmente.</p>
 */
@Component
@ConditionalOnProperty(name = "hospedagem.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ResidenciaRepository residenciaRepository;
    private final ClienteRepository clienteRepository;
    private final QuartoFactory quartoFactory;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(ResidenciaRepository residenciaRepository,
                      ClienteRepository clienteRepository,
                      QuartoFactory quartoFactory,
                      PasswordEncoder passwordEncoder) {
        this.residenciaRepository = residenciaRepository;
        this.clienteRepository = clienteRepository;
        this.quartoFactory = quartoFactory;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (residenciaRepository.count() > 0 || clienteRepository.count() > 0) {
            log.info("Seed ignorado: ja existem dados no banco.");
            return;
        }

        Residencia residencia = new Residencia(
                "Rua das Piscinas Naturais", "123", "Barra Grande",
                "45520-000", "(73) 99999-0000", "pousada.marau@example.com");

        Quarto individual = quartoFactory.criarIndividual("101", new BigDecimal("150.00"), true, false);
        Quarto casal = quartoFactory.criarCasal("201", new BigDecimal("250.00"), true, true);
        residencia.adicionarQuarto(individual);
        residencia.adicionarQuarto(casal);
        residenciaRepository.save(residencia);

        Cliente cliente = new Cliente(
                "Joao da Silva", "529.982.247-25", "Av. Brasil, 1000 - Centro",
                "(31) 98888-1111", "joao.silva@example.com",
                passwordEncoder.encode("senha123"));
        clienteRepository.save(cliente);

        log.info("Seed concluido: 1 residencia com 2 quartos e 1 cliente criados.");
    }
}
