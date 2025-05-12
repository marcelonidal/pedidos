package br.com.fiap.pedido.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoDTO(
        UUID produtoId,
        int quantidade,
        BigDecimal precoUnitario
) {}