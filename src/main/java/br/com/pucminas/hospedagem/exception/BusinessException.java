package br.com.pucminas.hospedagem.exception;

/**
 * Excecao para violacoes de regras de negocio (ex.: datas invalidas,
 * operacao nao permitida). Resulta em resposta HTTP 400.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
