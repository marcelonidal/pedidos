package br.com.fiap.pedido.app.dto.pedido;

import java.util.List;
import java.util.UUID;

public record PedidoRequestDTO(
        UUID clienteId,
        List<ItemPedidoDTO> itens,
        UUID idPagamento
) {}