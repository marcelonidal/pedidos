package br.com.fiap.pedido.core.domain.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {
    public ProdutoNaoEncontradoException(String msg) {
      super(msg);
    }
}
