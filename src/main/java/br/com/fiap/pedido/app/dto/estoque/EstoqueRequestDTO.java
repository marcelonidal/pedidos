package br.com.fiap.pedido.app.dto.estoque;

import java.io.Serializable;
import java.util.UUID;

public record EstoqueRequestDTO(
        UUID produtoId,
        int quantidade
) implements Serializable {}
