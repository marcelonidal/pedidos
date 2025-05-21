package br.com.fiap.pedido.app.dto.pedido;

import java.io.Serializable;
import java.util.List;

public record PedidoRequestDTO(
        String clienteCpf,
        List<ItemPedidoDTO> itens,
        String idCartao
) implements Serializable {}