package br.com.fiap.pedido.infra.service;

import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.app.mapper.PedidoMapper;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.usecase.PedidoService;
import br.com.fiap.pedido.infra.repository.postgres.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;

    @Override
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        Pedido pedido = mapper.toModel(dto);
        Pedido salvo = repository.save(pedido);
        return mapper.toResponse(salvo);
    }

    @Override
    public PedidoResponseDTO buscarPorId(UUID id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));
        return mapper.toResponse(pedido);
    }

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelarPedido(UUID id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));
        pedido.setStatus(br.com.fiap.pedido.core.domain.model.PedidoStatus.CANCELADO);
        repository.save(pedido);
    }

}
