package br.com.fiap.pedido.core.domain.exception.unit;

import br.com.fiap.pedido.core.domain.exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionsTest {

    @Test
    void shouldThrowClienteNaoEncontradoException() {
        ClienteNaoEncontradoException ex = new ClienteNaoEncontradoException("Cliente 123 nao encontrado");
        assertEquals("Cliente 123 nao encontrado", ex.getMessage());
    }

    @Test
    void shouldThrowEstoqueInsuficienteException() {
        EstoqueInsuficienteException ex = new EstoqueInsuficienteException("Estoque insuficiente para o produto X");
        assertEquals("Estoque insuficiente para o produto X", ex.getMessage());
    }

    @Test
    void shouldThrowPagamentoException() {
        PagamentoException ex = new PagamentoException("Erro ao processar pagamento");
        assertEquals("Erro ao processar pagamento", ex.getMessage());
    }

    @Test
    void shouldThrowPedidoNaoEncontradoException() {
        PedidoNaoEncontradoException ex = new PedidoNaoEncontradoException("Pedido 999 nao encontrado");
        assertEquals("Pedido 999 nao encontrado", ex.getMessage());
    }

    @Test
    void shouldThrowProdutoNaoEncontradoException() {
        ProdutoNaoEncontradoException ex = new ProdutoNaoEncontradoException("Produto ABC nao encontrado");
        assertEquals("Produto ABC nao encontrado", ex.getMessage());
    }

}
