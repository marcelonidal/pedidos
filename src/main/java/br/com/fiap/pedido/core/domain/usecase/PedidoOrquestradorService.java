package br.com.fiap.pedido.core.domain.usecase;

import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;

import java.util.UUID;

public interface PedidoOrquestradorService {
    PedidoResponseDTO criarPedido(PedidoRequestDTO dto);
    PedidoResponseDTO buscarPedidoPorId(UUID id);
}