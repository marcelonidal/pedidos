package br.com.fiap.pedido.infra.service;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pagamento.PagamentoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.dto.produto.ProdutoResponseDTO;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
        clienteClient.validarCliente(dto.clienteCpf());
        Map<UUID, ProdutoResponseDTO> produtos = produtoClient.buscarProdutos(dto.itens());
        estoqueClient.abaterEstoque(dto.itens());

        Pedido pedido = pedidoMapper.toModel(dto);
        List<ItemPedido> itens = mapearItens(dto.itens(), produtos, pedido);
        pedido.setItens(itens);
        pedido.setValorTotal(calcularTotal(itens));
        Pedido salvo = repository.save(pedido);

        // Dispara solicitacao de pagamento
        PagamentoRequestDTO pagamentoRequest = new PagamentoRequestDTO(
                salvo.getId(),
                dto.idCartao(),
                salvo.getValorTotal()
        );
        PagamentoDTO pagamento = pagamentoClient.solicitarPagamento(pagamentoRequest);

        // Monta o DTO completo de resposta
        PedidoResponseDTO response = pedidoMapper.toResponse(salvo, pagamento);

        // Publica o evento com base no próprio response
        eventPublisher.publicarPedidoCriado(response);

        // Atualiza o status após publicação
        PedidoStatus status;
        if (pagamento.status() == StatusPagamento.AGUARDANDO) {
            status = PedidoStatus.ENVIADO; // foi enviado pra fila
        } else {
            status = mapearStatusPagamentoParaPedido(pagamento.status());
        }
        salvo.setStatus(status);

        repository.save(salvo);

        return pedidoMapper.toResponse(salvo, pagamento);
    }

    @Override
    public PedidoResponseDTO buscarPorId(UUID id) {
        Pedido pedido = repository.buscarItensPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("ID " + id + " não encontrada"));

        PagamentoDTO pagamento = pagamentoClient.consultarStatus(pedido.getId());

        if (pagamento != null && pagamento.status() != null) {
            PedidoStatus statusAtual = pedido.getStatus();

            if (statusAtual != PedidoStatus.CANCELADO && statusAtual != PedidoStatus.REPROVADO) {
                PedidoStatus statusPagamento = mapearStatusPagamentoParaPedido(pagamento.status());

                if (!statusAtual.equals(statusPagamento)) {
                    pedido.setStatus(statusPagamento);

                    // Se o pagamento foi confirmado e ainda nao tem data registrada, seta agora
                    if (statusPagamento == PedidoStatus.PAGO && pagamento.dataAprovacao() == null) {
                        pagamento = new PagamentoDTO(
                                pedido.getId(),
                                pagamento.idCartao(),
                                pagamento.valor(),
                                pagamento.status(),
                                LocalDateTime.now()
                        );
                    }

                    Pedido atualizado = repository.save(pedido);

                    PedidoResponseMongoDTO evento = pedidoMongoMapper.toResponse(atualizado, pagamento);
                    eventPublisher.publicarAtualizacaoPedido(evento);
                }
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
                            p.getId(),
                            p.getIdCartao(),
                            p.getValorTotal(),
                            StatusPagamento.AGUARDANDO,
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
                            p.getId(),
                            p.getIdCartao(),
                            p.getValorTotal(),
                            StatusPagamento.AGUARDANDO,
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
        if (novo.clienteCpf() != null && !novo.clienteCpf().equals(antigo.getClienteCpf())) {
            antigo.setClienteCpf(novo.clienteCpf());
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
            Map<UUID, ProdutoResponseDTO> produtos = produtoClient.buscarProdutos(novo.itens());
            List<ItemPedido> novosItens = mapearItens(novo.itens(), produtos, antigo);
            antigo.setItens(novosItens);
            antigo.setValorTotal(calcularTotal(novosItens));
        }

        if (novo.pagamento() != null && novo.pagamento().idCartao() != null &&
                !novo.pagamento().idCartao().equals(antigo.getIdCartao())) {
            antigo.setIdCartao(novo.pagamento().idCartao());
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
        }

        return true;
    }

    private List<ItemPedido> mapearItens(List<ItemPedidoDTO> itensDTO, Map<UUID, ProdutoResponseDTO> produtos, Pedido pedidoPai) {
        return itensDTO.stream()
                .map(dto -> {
                    ProdutoResponseDTO produto = produtos.get(dto.produtoId());

                    return ItemPedido.builder()
                            .produtoId(dto.produtoId())
                            .quantidade(dto.quantidade())
                            .precoUnitario(produto.preco())
                            .pedido(pedidoPai)
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PedidoStatus mapearStatusPagamentoParaPedido(StatusPagamento statusPagamento) {
        return switch (statusPagamento) {
            case AGUARDANDO -> PedidoStatus.CRIADO;
            case APROVADO -> PedidoStatus.PAGO;
            case RECUSADO -> PedidoStatus.REPROVADO;
        };
    }

    private BigDecimal calcularTotal(List<ItemPedido> itens) {
        return itens.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}