package br.com.fiap.pedido.core.domain.exception;

public class PedidoNaoEncontradoException  extends RuntimeException {
    public PedidoNaoEncontradoException(String msg) {
        super(msg);
    }
}
