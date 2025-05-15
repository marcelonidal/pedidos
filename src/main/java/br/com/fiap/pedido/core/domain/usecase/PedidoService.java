package br.com.fiap.pedido.core.domain.usecase;

import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
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

    void atualizarStatus(UUID id, PedidoStatus novoStatus);

    void cancelarPedido(UUID id);
}