package br.com.fiap.pedido.core.domain.model.unit;

import br.com.fiap.pedido.core.domain.model.ItemPedido;
import br.com.fiap.pedido.core.domain.model.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemPedidoTest {

    @Test
    void shouldCreateItemPedidoWithAllFields() {
        UUID produtoId = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();

        ItemPedido item = ItemPedido.builder()
                .id(UUID.randomUUID())
                .produtoId(produtoId)
                .quantidade(3)
                .precoUnitario(new BigDecimal("29.90"))
                .pedido(pedido)
                .build();

        assertNotNull(item.getId());
        assertEquals(produtoId, item.getProdutoId());
        assertEquals(3, item.getQuantidade());
        assertEquals(new BigDecimal("29.90"), item.getPrecoUnitario());
        assertEquals(pedido, item.getPedido());
    }

    @Test
    void shouldAllowChangingPedidoReference() {
        ItemPedido item = new ItemPedido();
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();

        item.setPedido(pedido);

        assertEquals(pedido, item.getPedido());
    }

}
