package br.com.fiap.pedido.infra.service.unit;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        UUID clienteId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        List<ItemPedidoDTO> itens = List.of(
                new ItemPedidoDTO(produtoId, 2, new BigDecimal("50.00"))
        );

        PedidoRequestDTO dto = new PedidoRequestDTO(clienteId, itens, UUID.randomUUID());

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteId(clienteId)
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(new BigDecimal("100.00"))
                .itens(Collections.emptyList())
                .build();

        PagamentoDTO pagamento = new PagamentoDTO(
                dto.idPagamento(),
                StatusPagamento.AGUARDANDO,
                null,
                null
        );

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedidoId,
                clienteId,
                pedido.getDataCriacao(),
                "CRIADO",
                new BigDecimal("100.00"),
                itens,
                pagamento
        );

        when(mapper.toModel(dto)).thenReturn(pedido);
        when(repository.save(pedido)).thenReturn(pedido);
        when(mapper.toResponse(pedido, pagamento)).thenReturn(responseMock);

        doNothing().when(clienteClient).validarCliente(clienteId);
        doNothing().when(produtoClient).validarProdutos(itens);
        doNothing().when(estoqueClient).abaterEstoque(itens);
        doNothing().when(eventPublisher).publicarPedidoCriado(any());

        PedidoResponseDTO response = service.criarPedido(dto);

        assertNotNull(response);
        assertEquals("CRIADO", response.status());
        assertEquals(new BigDecimal("100.00"), response.valorTotal());
        assertEquals(StatusPagamento.AGUARDANDO, response.pagamento().statusPagamento());

        verify(repository, times(2)).save(pedido); // validando os dois saves
        verify(eventPublisher).publicarPedidoCriado(any());
    }

    @Test
    void shouldReturnPedidoWhenIdExists() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();

        // pagamento aprovado -> status do pedido sera PAGO
        StatusPagamento statusPagamento = StatusPagamento.APROVADO;
        PedidoStatus statusEsperado = PedidoStatus.PAGO;

        PagamentoDTO pagamento = new PagamentoDTO(
                pagamentoId,
                statusPagamento,
                "CARTAO_CREDITO",
                LocalDateTime.now()
        );

        Pedido pedido = Pedido.builder()
                .id(id)
                .clienteId(clienteId)
                .dataCriacao(LocalDateTime.now())
                .status(statusEsperado)
                .valorTotal(new BigDecimal("100.00"))
                .idPagamento(pagamentoId)
                .itens(Collections.emptyList())
                .build();

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                id,
                clienteId,
                pedido.getDataCriacao(),
                statusEsperado.name(),
                pedido.getValorTotal(),
                List.of(),
                pagamento
        );

        when(repository.buscarItensPorId(id)).thenReturn(Optional.of(pedido));
        when(pagamentoClient.consultarStatus(pagamentoId)).thenReturn(pagamento);
        when(mapper.toResponse(pedido, pagamento)).thenReturn(responseMock);

        PedidoResponseDTO response = service.buscarPorId(id);

        assertNotNull(response);
        assertEquals(statusEsperado.name(), response.status());
        assertEquals(statusPagamento, response.pagamento().statusPagamento());

        verify(repository).buscarItensPorId(id);
        verify(pagamentoClient).consultarStatus(pagamentoId);
    }

    @Test
    void shouldThrowExceptionWhenPedidoNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.buscarItensPorId(id)).thenReturn(Optional.empty());

        assertThrows(PedidoNaoEncontradoException.class, () -> service.buscarPorId(id));

        //valida se o metodo foi realmente chamado
        verify(repository).buscarItensPorId(id);
    }

    @Test
    void shouldReturnAllPedidos() {
        UUID pedidoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteId(clienteId)
                .idPagamento(pagamentoId) // <<< ESSENCIAL
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(new BigDecimal("50.00"))
                .itens(Collections.emptyList())
                .build();

        List<Pedido> pedidos = List.of(pedido);

        List<ItemPedidoDTO> itens = List.of(); // Simulado ou vazio

        PagamentoDTO pagamento = new PagamentoDTO(
                pagamentoId,
                StatusPagamento.AGUARDANDO,
                null,
                null
        );

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedidoId,
                clienteId,
                pedido.getDataCriacao(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                List.of(), // ou os itens mockados
                pagamento
        );

        when(repository.buscarItens()).thenReturn(List.of(pedido));
        when(mapper.toResponse(pedido, pagamento)).thenReturn(responseMock);

        List<PedidoResponseDTO> result = service.listarTodos();

        assertEquals(1, result.size());
        assertEquals(pedidoId, result.getFirst().id());
        assertEquals(StatusPagamento.AGUARDANDO, result.getFirst().pagamento().statusPagamento());

        // valida se o metodo foi realmente chamado
        when(repository.buscarItens()).thenReturn(List.of(pedido));
    }

    @Test
    void shouldCancelPedidoById() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        Pedido pedido = Pedido.builder()
                .id(id)
                .clienteId(clienteId)
                .status(PedidoStatus.CRIADO)
                .dataCriacao(LocalDateTime.now())
                .valorTotal(new BigDecimal("150.00"))
                .itens(Collections.emptyList())
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(pedido));

        service.cancelarPedido(id);

        assertEquals(PedidoStatus.CANCELADO, pedido.getStatus());

        // valida se o metodo foi realmente chamado
        verify(repository).findById(id);
        verify(repository).save(pedido);
    }

}