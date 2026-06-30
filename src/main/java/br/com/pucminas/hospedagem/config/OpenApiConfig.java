package br.com.pucminas.hospedagem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentacao OpenAPI/Swagger da API REST.
 *
 * <p>Apos iniciar a aplicacao, a documentacao interativa fica disponivel em
 * {@code http://localhost:8080/swagger-ui.html}.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospedagemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Hospedagem - API REST")
                        .description("API para gerenciamento de residencias, quartos, clientes, "
                                + "reservas e alugueis. Trabalho Pratico de Programacao Modular - PUC Minas.")
                        .version("1.0.0")
                        .contact(new Contact().name("PUC Minas - Engenharia de Software"))
                        .license(new License().name("Uso academico")));
    }
}
