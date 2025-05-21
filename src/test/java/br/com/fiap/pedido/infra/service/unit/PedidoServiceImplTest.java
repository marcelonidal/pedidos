package br.com.fiap.pedido.infra.service.unit;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.produto.ProdutoResponseDTO;
import br.com.fiap.pedido.app.event.PedidoEventPublisher;
import br.com.fiap.pedido.app.mapper.PedidoMapper;
import br.com.fiap.pedido.app.mapper.PedidoMongoMapper;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import br.com.fiap.pedido.infra.client.ClienteClient;
import br.com.fiap.pedido.infra.client.EstoqueClient;
import br.com.fiap.pedido.infra.client.PagamentoClient;
import br.com.fiap.pedido.infra.client.ProdutoClient;
import br.com.fiap.pedido.infra.repository.postgres.PedidoRepository;
import br.com.fiap.pedido.infra.service.PedidoServiceImpl;
import br.com.fiap.pedido.util.GeradorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceImplTest {

    private PedidoRepository repository;
    private PedidoMapper mapper;
    private ClienteClient clienteClient;
    private ProdutoClient produtoClient;
    private EstoqueClient estoqueClient;
    private PagamentoClient pagamentoClient;
    private PedidoEventPublisher eventPublisher;

    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRepository.class);
        mapper = mock(PedidoMapper.class);
        PedidoMongoMapper pedidoMongoMapper = mock(PedidoMongoMapper.class);
        clienteClient = mock(ClienteClient.class);
        produtoClient = mock(ProdutoClient.class);
        estoqueClient = mock(EstoqueClient.class);
        pagamentoClient = mock(PagamentoClient.class);
        eventPublisher = mock(PedidoEventPublisher.class);

        service = new PedidoServiceImpl(
                repository,
                mapper,
                pedidoMongoMapper,
                clienteClient,
                produtoClient,
                estoqueClient,
                pagamentoClient,
                eventPublisher
        );
    }

    @Test
    void shouldCreatePedidoWithExpectedStatusAndTotal() {
        UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        List<ItemPedidoDTO> itens = List.of(
                new ItemPedidoDTO(produtoId, 2, BigDecimal.valueOf(50))
        );

        PedidoRequestDTO dto = new PedidoRequestDTO(
                GeradorUtil.gerarCpfValido(),
                itens,
                GeradorUtil.gerarNumeroCartaoValido()
        );

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteCpf(dto.clienteCpf())
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(BigDecimal.valueOf(100))
                .itens(Collections.emptyList())
                .idCartao(dto.idCartao())
                .build();

        PagamentoDTO pagamento = new PagamentoDTO(
                pedido.getId(),
                pedido.getIdCartao(),
                BigDecimal.ZERO,
                StatusPagamento.AGUARDANDO,
                null
        );

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedidoId,
                pedido.getClienteCpf(),
                pedido.getDataCriacao(),
                "CRIADO",
                BigDecimal.valueOf(100),
                itens,
                pagamento
        );

        when(mapper.toModel(dto)).thenReturn(pedido);
        when(repository.save(pedido)).thenReturn(pedido);
        when(mapper.toResponse(pedido, pagamento)).thenReturn(responseMock);

        doNothing().when(clienteClient).validarCliente(pedido.getClienteCpf());

        Map<UUID, ProdutoResponseDTO> produtosMock = Map.of(
                produtoId, new ProdutoResponseDTO(
                        produtoId,
                        "Produto Teste",
                        "Marca X",
                        "Azul",
                        "M",
                        "Adulto",
                        10,
                        new BigDecimal("50.00"),
                        "SKU-1234"
                )
        );
        when(produtoClient.buscarProdutos(itens)).thenReturn(produtosMock);
        doNothing().when(estoqueClient).abaterEstoque(itens);
        when(pagamentoClient.solicitarPagamento(any())).thenReturn(pagamento);
        doNothing().when(eventPublisher).publicarPedidoCriado(any());

        PedidoResponseDTO response = service.criarPedido(dto);

        assertNotNull(response);
        assertEquals("CRIADO", response.status());
        assertEquals(BigDecimal.valueOf(100), response.valorTotal());
        assertEquals(StatusPagamento.AGUARDANDO, response.pagamento().status());

        verify(repository, times(2)).save(pedido);
        verify(eventPublisher).publicarPedidoCriado(any());
    }

    @Test
    void shouldReturnPedidoWhenIdExists() {
        UUID id = UUID.randomUUID();

        Pedido pedido = Pedido.builder()
                .id(id)
                .clienteCpf(GeradorUtil.gerarCpfValido())
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(BigDecimal.valueOf(200))
                .idCartao(GeradorUtil.gerarNumeroCartaoValido())
                .itens(Collections.emptyList())
                .build();

        PagamentoDTO pagamento = new PagamentoDTO(
                pedido.getId(),
                pedido.getIdCartao(),
                BigDecimal.valueOf(200),
                StatusPagamento.APROVADO,
                LocalDateTime.now()
        );

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteCpf(),
                pedido.getDataCriacao(),
                PedidoStatus.PAGO.name(),
                pedido.getValorTotal(),
                List.of(),
                pagamento
        );

        when(repository.buscarItensPorId(id)).thenReturn(Optional.of(pedido));
        when(pagamentoClient.consultarStatus(pedido.getId())).thenReturn(pagamento);
        when(repository.save(any())).thenReturn(pedido);
        when(mapper.toResponse(pedido, pagamento)).thenReturn(responseMock);

        PedidoResponseDTO response = service.buscarPorId(id);

        assertNotNull(response);
        assertEquals(PedidoStatus.PAGO.name(), response.status());
        assertEquals(StatusPagamento.APROVADO, response.pagamento().status());
    }

    @Test
    void shouldThrowExceptionWhenPedidoNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.buscarItensPorId(id)).thenReturn(Optional.empty());

        assertThrows(PedidoNaoEncontradoException.class, () -> service.buscarPorId(id));

        verify(repository).buscarItensPorId(id);
    }

    @Test
    void shouldReturnAllPedidos() {
        UUID pedidoId = UUID.randomUUID();

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteCpf(GeradorUtil.gerarCpfValido())
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(BigDecimal.valueOf(50))
                .idCartao(GeradorUtil.gerarNumeroCartaoValido())
                .itens(Collections.emptyList())
                .build();

        PagamentoDTO pagamento = new PagamentoDTO(
                pedidoId,
                pedido.getIdCartao(),
                BigDecimal.valueOf(50),
                StatusPagamento.AGUARDANDO,
                null
        );

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedidoId,
                pedido.getClienteCpf(),
                pedido.getDataCriacao(),
                PedidoStatus.CRIADO.name(),
                pedido.getValorTotal(),
                List.of(),
                pagamento
        );

        when(repository.buscarItens()).thenReturn(List.of(pedido));
        when(mapper.toResponse(pedido, pagamento)).thenReturn(responseMock);

        List<PedidoResponseDTO> result = service.listarTodos();

        assertEquals(1, result.size());
        PedidoResponseDTO primeiro = result.getFirst();
        assertEquals(pedidoId, primeiro.id());
        assertEquals(StatusPagamento.AGUARDANDO, primeiro.pagamento().status());

        verify(repository).buscarItens();
    }

    @Test
    void shouldCancelPedidoById() {
        UUID id = UUID.randomUUID();

        Pedido pedido = Pedido.builder()
                .id(id)
                .clienteCpf(GeradorUtil.gerarCpfValido())
                .status(PedidoStatus.CRIADO)
                .dataCriacao(LocalDateTime.now())
                .valorTotal(BigDecimal.valueOf(150))
                .itens(Collections.emptyList())
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(pedido));

        service.cancelarPedido(id);

        assertEquals(PedidoStatus.CANCELADO, pedido.getStatus());
        verify(repository).findById(id);
        verify(repository).save(pedido);
    }

}