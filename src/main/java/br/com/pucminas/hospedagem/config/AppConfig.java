package br.com.pucminas.hospedagem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracoes gerais da aplicacao.
 *
 * <p>Disponibiliza o {@link PasswordEncoder} (BCrypt) usado para armazenar
 * as senhas dos clientes de forma segura (hash), atendendo ao requisito de
 * autenticacao de clientes.</p>
 *
 * <p>Os beans declarados aqui sao, por padrao, gerenciados pelo Spring como
 * <b>Singleton</b> (padrao de projeto Singleton aplicado pelo container).</p>
 */
@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
