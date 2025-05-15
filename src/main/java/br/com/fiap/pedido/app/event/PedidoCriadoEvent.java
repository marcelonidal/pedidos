package br.com.fiap.pedido.app.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PedidoCriadoEvent(
        UUID pedidoId,
        UUID clienteId,
        BigDecimal valorTotal,
        List<ItemPedidoPayload> itens
) {
    public record ItemPedidoPayload(
            UUID produtoId,
            int quantidade,
            BigDecimal precoUnitario
    ) {}
}