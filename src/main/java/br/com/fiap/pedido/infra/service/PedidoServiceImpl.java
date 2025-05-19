package br.com.fiap.pedido.infra.service;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.event.PedidoEventPublisher;
import br.com.fiap.pedido.app.mapper.PedidoMapper;
import br.com.fiap.pedido.app.mapper.PedidoMongoMapper;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import br.com.fiap.pedido.core.domain.model.ItemPedido;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final PedidoMapper pedidoMapper;
    private final PedidoMongoMapper pedidoMongoMapper;
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

        Pedido pedido = pedidoMapper.toModel(dto);
        Pedido salvo = repository.save(pedido);

        // Status de pagamento inicial
        PagamentoDTO pagamento = new PagamentoDTO(
                dto.idPagamento(),
                StatusPagamento.AGUARDANDO,
                null,
                null
        );

        // Monta o DTO completo de resposta
        PedidoResponseDTO response = pedidoMapper.toResponse(salvo, pagamento);

        // Publica o evento com base no próprio response
        eventPublisher.publicarPedidoCriado(response);

        // Atualiza o status para ENVIADO após publicação
        salvo.setStatus(PedidoStatus.ENVIADO);
        repository.save(salvo);

        return pedidoMapper.toResponse(salvo, pagamento);
    }

    @Override
    public PedidoResponseDTO buscarPorId(UUID id) {
        Pedido pedido = repository.buscarItensPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));

        PagamentoDTO pagamento = pagamentoClient.consultarStatus(pedido.getIdPagamento());

        if (pagamento != null && pagamento.statusPagamento() != null) {
            PedidoStatus statusAtual = pedido.getStatus();
            PedidoStatus statusPagamento = mapearStatusPagamentoParaPedido(pagamento.statusPagamento());

            if (!statusAtual.equals(statusPagamento)) {
                pedido.setStatus(statusPagamento);

                // Se o pagamento foi confirmado e ainda nao tem data registrada, seta agora
                if (statusPagamento == PedidoStatus.PAGO && pagamento.dataPagamento() == null) {
                    pagamento = new PagamentoDTO(
                            pagamento.id(),
                            pagamento.statusPagamento(),
                            pagamento.metodoPagamento(),
                            LocalDateTime.now()
                    );
                }

                Pedido atualizado = repository.save(pedido);

                PedidoResponseMongoDTO evento = pedidoMongoMapper.toResponse(atualizado, pagamento);
                eventPublisher.publicarAtualizacaoPedido(evento);
            }
        }

        return pedidoMapper.toResponse(pedido, pagamento);
    }

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        return repository.buscarItens()
                .stream()
                .map(p -> {
                    PagamentoDTO pagamento = new PagamentoDTO(
                            p.getIdPagamento(),
                            StatusPagamento.AGUARDANDO,
                            null,
                            null
                    );
                    return pedidoMapper.toResponse(p, pagamento);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<PedidoResponseDTO> listarPaginado(Pageable pageable) {
        return repository.findAll(pageable)
                .map(p -> {
                    PagamentoDTO pagamento = new PagamentoDTO(
                            p.getIdPagamento(),
                            StatusPagamento.AGUARDANDO,
                            null,
                            null
                    );
                    return pedidoMapper.toResponse(p, pagamento);
                });
    }

    @Override
    public void atualizarPedido(PedidoResponseDTO pedidoResponseDTO) {
        Pedido pedido = repository.findById(pedidoResponseDTO.id())
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + pedidoResponseDTO.id() + " nao encontrado"));

        compararAlteracoes(pedido, pedidoResponseDTO);

        repository.save(pedido);
    }

    @Override
    public void cancelarPedido(UUID id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));
        pedido.setStatus(br.com.fiap.pedido.core.domain.model.PedidoStatus.CANCELADO);
        repository.save(pedido);
    }

    private void compararAlteracoes(Pedido antigo, PedidoResponseDTO novo) {
        if (novo.clienteId() != null && !novo.clienteId().equals(antigo.getClienteId())) {
            antigo.setClienteId(novo.clienteId());
        }

        if (novo.dataCriacao() != null && !novo.dataCriacao().equals(antigo.getDataCriacao())) {
            antigo.setDataCriacao(novo.dataCriacao());
        }

        if (novo.status() != null && !antigo.getStatus().name().equals(novo.status())) {
            antigo.setStatus(PedidoStatus.valueOf(novo.status()));
        }

        if (novo.valorTotal() != null && !novo.valorTotal().equals(antigo.getValorTotal())) {
            antigo.setValorTotal(novo.valorTotal());
        }

        if (novo.itens() != null && !equalsItens(novo.itens(), antigo.getItens())) {
            antigo.setItens(mapearItens(novo.itens(), antigo));
        }

        if (novo.pagamento() != null && novo.pagamento().id() != null &&
                !novo.pagamento().id().equals(antigo.getIdPagamento())) {
            antigo.setIdPagamento(novo.pagamento().id());
        }
    }

    private boolean equalsItens(List<ItemPedidoDTO> novos, List<ItemPedido> antigos) {
        if (novos == null || antigos == null) return false;
        if (novos.size() != antigos.size()) return false;

        for (int i = 0; i < novos.size(); i++) {
            ItemPedidoDTO dto = novos.get(i);
            ItemPedido entity = antigos.get(i);

            if (!Objects.equals(dto.produtoId(), entity.getProdutoId())) return false;
            if (dto.quantidade() != entity.getQuantidade()) return false;
            if (dto.precoUnitario() == null || entity.getPrecoUnitario() == null) return false;
            if (dto.precoUnitario().compareTo(entity.getPrecoUnitario()) != 0) return false;
        }

        return true;
    }

    private List<ItemPedido> mapearItens(List<ItemPedidoDTO> itensDTO, Pedido pedidoPai) {
        return itensDTO.stream()
                .map(dto -> ItemPedido.builder()
                        .produtoId(dto.produtoId())
                        .quantidade(dto.quantidade())
                        .precoUnitario(dto.precoUnitario())
                        .pedido(pedidoPai)
                        .build())
                .toList();
    }

    private PedidoStatus mapearStatusPagamentoParaPedido(StatusPagamento statusPagamento) {
        return switch (statusPagamento) {
            case AGUARDANDO -> PedidoStatus.CRIADO;
            case APROVADO -> PedidoStatus.PAGO;
            case RECUSADO -> PedidoStatus.REPROVADO;
        };
    }

}