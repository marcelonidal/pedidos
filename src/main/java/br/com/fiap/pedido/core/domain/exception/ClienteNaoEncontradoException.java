package br.com.fiap.pedido.core.domain.exception;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String  msg) {
        super(msg);
    }
}
