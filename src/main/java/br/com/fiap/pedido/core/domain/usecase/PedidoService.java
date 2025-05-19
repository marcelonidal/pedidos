package br.com.fiap.pedido.core.domain.usecase;

import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PedidoService {

    PedidoResponseDTO criarPedido(PedidoRequestDTO dto);

    PedidoResponseDTO buscarPorId(UUID id);

    List<PedidoResponseDTO> listarTodos();

    Page<PedidoResponseDTO> listarPaginado(Pageable pageable);

    void atualizarPedido(PedidoResponseDTO pedidoResponseDTO);

    void cancelarPedido(UUID id);
}