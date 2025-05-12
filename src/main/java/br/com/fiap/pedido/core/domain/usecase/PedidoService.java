package br.com.fiap.pedido.core.domain.usecase;

import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PedidoService {

    PedidoResponseDTO criarPedido(PedidoRequestDTO dto);

    PedidoResponseDTO buscarPorId(UUID id);

    List<PedidoResponseDTO> listarTodos();

    void cancelarPedido(UUID id);
}
