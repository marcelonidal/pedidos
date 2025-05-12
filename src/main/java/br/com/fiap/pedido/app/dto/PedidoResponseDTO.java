package br.com.fiap.pedido.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        UUID clienteId,
        LocalDateTime dataCriacao,
        String status,
        BigDecimal valorTotal,
        List<ItemPedidoDTO> itens
) {}