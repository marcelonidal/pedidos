package br.com.fiap.pedido.app.dto.pedido;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoDTO(
        UUID produtoId,
        int quantidade,
        BigDecimal precoUnitario
) implements Serializable {}