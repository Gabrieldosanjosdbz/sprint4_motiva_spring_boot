package br.com.motiva.exception;

/** Requisição bem formada, mas que fere uma regra do domínio (vira 400). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
