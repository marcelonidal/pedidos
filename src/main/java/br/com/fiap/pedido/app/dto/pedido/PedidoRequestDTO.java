package br.com.fiap.pedido.app.dto.pedido;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record PedidoRequestDTO(
        String clienteCpf,
        List<ItemPedidoDTO> itens,
        UUID idPagamento,
        String idCartao
) implements Serializable {}