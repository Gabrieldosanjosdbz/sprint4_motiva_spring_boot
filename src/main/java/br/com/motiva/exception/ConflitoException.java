package br.com.motiva.exception;

/** Conflito com o estado atual do banco: duplicidade ou registro ainda referenciado (vira 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
