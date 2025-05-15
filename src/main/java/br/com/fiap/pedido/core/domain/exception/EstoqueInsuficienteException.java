package br.com.fiap.pedido.core.domain.exception;

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(String  msg) {
      super(msg);
    }
}
