package br.com.fiap.pedido.infra.service;

import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.StatusPagamentoResponseDTO;
import br.com.fiap.pedido.app.event.PedidoCriadoEvent;
import br.com.fiap.pedido.app.event.PedidoEventPublisher;
import br.com.fiap.pedido.app.mapper.PedidoMapper;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.core.domain.usecase.PedidoService;
import br.com.fiap.pedido.infra.client.ClienteClient;
import br.com.fiap.pedido.infra.client.EstoqueClient;
import br.com.fiap.pedido.infra.client.PagamentoClient;
import br.com.fiap.pedido.infra.client.ProdutoClient;
import br.com.fiap.pedido.infra.repository.postgres.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;
    private final ClienteClient clienteClient;
    private final ProdutoClient produtoClient;
    private final EstoqueClient estoqueClient;
    private final PagamentoClient pagamentoClient;
    private final PedidoEventPublisher eventPublisher;

    @Override
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        clienteClient.validarCliente(dto.clienteId());
        produtoClient.validarProdutos(dto.itens());
        estoqueClient.abaterEstoque(dto.itens());

        Pedido pedido = mapper.toModel(dto);
        Pedido salvo = repository.save(pedido);

        List<PedidoCriadoEvent.ItemPedidoPayload> itens = salvo.getItens().stream()
                .map(i -> new PedidoCriadoEvent.ItemPedidoPayload(
                        i.getProdutoId(),
                        i.getQuantidade(),
                        i.getPrecoUnitario()))
                .toList();

        PedidoCriadoEvent evento = new PedidoCriadoEvent(
                salvo.getId(),
                salvo.getClienteId(),
                salvo.getValorTotal(),
                itens
        );

        eventPublisher.publicarPedidoCriado(evento); // envia para a fila

        salvo.setStatus(PedidoStatus.ENVIADO);       // atualiza status
        repository.save(salvo);                      // salva novamente

        return mapper.toResponse(salvo);
    }

    @Override
    public PedidoResponseDTO buscarPorId(UUID id) {
        Pedido pedido = repository.buscarItensPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));

        StatusPagamentoResponseDTO pagamento = pagamentoClient.consultarStatus(id);

        if (pagamento != null && !pagamento.status().equals(pedido.getStatus().name())) {
            pedido.setStatus(PedidoStatus.valueOf(pagamento.status()));
            repository.save(pedido);
        }

        return mapper.toResponse(pedido);
    }

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        return repository.buscarItens()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PedidoResponseDTO> listarPaginado(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }

    @Override
    public void atualizarStatus(UUID id, PedidoStatus novoStatus) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " nao encontrado"));

        pedido.setStatus(novoStatus);
        repository.save(pedido);
    }

    @Override
    public void cancelarPedido(UUID id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));
        pedido.setStatus(br.com.fiap.pedido.core.domain.model.PedidoStatus.CANCELADO);
        repository.save(pedido);
    }

}