package br.com.pucminas.hospedagem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicacao Spring Boot do Sistema de Hospedagem.
 *
 * <p>Trabalho Pratico da disciplina de Programacao Modular -
 * Bacharelado em Engenharia de Software (PUC Minas).</p>
 */
@SpringBootApplication
public class HospedagemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospedagemApplication.class, args);
    }
}
