package br.com.pucminas.hospedagem.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Excecao lancada quando se tenta alugar ou reservar um quarto que ja esta
 * ocupado no periodo solicitado (regra de negocio 3).
 * Resulta em resposta HTTP 409 (conflito).
 */
public class QuartoIndisponivelException extends RuntimeException {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public QuartoIndisponivelException(Long quartoId, LocalDateTime entrada, LocalDateTime saida) {
        super("O quarto " + quartoId + " nao esta disponivel no periodo de "
                + entrada.format(FORMATO) + " a " + saida.format(FORMATO) + ".");
    }

    public QuartoIndisponivelException(String message) {
        super(message);
    }
}
