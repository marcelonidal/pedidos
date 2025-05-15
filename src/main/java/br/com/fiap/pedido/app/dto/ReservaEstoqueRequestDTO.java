package br.com.fiap.pedido.app.dto;

import java.util.UUID;

public record ReservaEstoqueRequestDTO(
        UUID produtoId,
        int quantidade
) {}
