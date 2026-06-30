package br.com.pucminas.hospedagem.exception;

/**
 * Excecao lancada quando uma entidade nao e encontrada.
 * Resulta em resposta HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " nao encontrado(a) com id " + id);
    }
}
