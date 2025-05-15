package br.com.fiap.pedido.app.dto;

import java.util.UUID;

public record StatusPagamentoResponseDTO(
        UUID pedidoId,
        String status
) {}