package br.com.pucminas.hospedagem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de fumaca: garante que todo o contexto da aplicacao (controllers,
 * services, repositorios, configuracoes) carrega corretamente usando o
 * banco H2 em memoria.
 */
@SpringBootTest
class HospedagemApplicationTests {

    @Test
    void contextLoads() {
        // Se o contexto Spring subir sem erros, a fiacao das camadas esta correta.
    }
}
