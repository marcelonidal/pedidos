package br.com.fiap.pedido.infra.service.unit;

import br.com.fiap.pedido.app.dto.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.app.event.PedidoEventPublisher;
import br.com.fiap.pedido.app.mapper.PedidoMapper;
import br.com.fiap.pedido.core.domain.exception.PedidoNaoEncontradoException;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceImplTest {

    private PedidoRepository repository;
    private PedidoMapper mapper;
    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRepository.class);
        mapper = mock(PedidoMapper.class);
        ClienteClient clienteClient = mock(ClienteClient.class);
        ProdutoClient produtoClient = mock(ProdutoClient.class);
        EstoqueClient estoqueClient = mock(EstoqueClient.class);
        PagamentoClient pagamentoClient = mock(PagamentoClient.class);
        PedidoEventPublisher eventPublisher = mock(PedidoEventPublisher.class);

        service = new PedidoServiceImpl(
                repository,
                mapper,
                clienteClient,
                produtoClient,
                estoqueClient,
                pagamentoClient,
                eventPublisher
        );
    }

    @Test
    void criarPedido_deveSalvarComTotalEStatus() {
        UUID clienteId = UUID.randomUUID();
        List<ItemPedidoDTO> itens = List.of(new ItemPedidoDTO(UUID.randomUUID(), 1, new BigDecimal("100.00")));
        PedidoRequestDTO dto = new PedidoRequestDTO(clienteId, itens);

        Pedido pedido = Pedido.builder()
                .id(UUID.randomUUID())
                .clienteId(clienteId)
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(new BigDecimal("100.00"))
                .build();

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getDataCriacao(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                itens
        );

        when(mapper.toModel(dto)).thenReturn(pedido);
        when(repository.save(pedido)).thenReturn(pedido);
        when(mapper.toResponse(pedido)).thenReturn(responseMock);

        PedidoResponseDTO response = service.criarPedido(dto);

        assertNotNull(response);
        assertEquals("CRIADO", response.status());
        verify(repository).save(pedido);
    }

    @Test
    void buscarPorId_deveRetornarPedido() {
        UUID id = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(id).build();

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                id, UUID.randomUUID(), LocalDateTime.now(), "CRIADO", new BigDecimal("100.00"), List.of()
        );

        when(repository.findById(id)).thenReturn(Optional.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(responseMock);

        PedidoResponseDTO response = service.buscarPorId(id);

        assertNotNull(response);
        verify(repository).findById(id);
    }

    @Test
    void buscarPorId_deveLancarExcecaoSeNaoEncontrar() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PedidoNaoEncontradoException.class, () -> service.buscarPorId(id));
    }

    @Test
    void listarTodos_deveRetornarListaDePedidos() {
        Pedido pedido = Pedido.builder().id(UUID.randomUUID()).build();
        List<Pedido> pedidos = List.of(pedido);

        PedidoResponseDTO responseMock = new PedidoResponseDTO(
                pedido.getId(), UUID.randomUUID(), LocalDateTime.now(), "CRIADO", new BigDecimal("50.00"), List.of()
        );

        when(repository.findAll()).thenReturn(pedidos);
        when(mapper.toResponse(pedido)).thenReturn(responseMock);

        List<PedidoResponseDTO> result = service.listarTodos();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void cancelarPedido_deveAtualizarStatusParaCancelado() {
        UUID id = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(id).status(PedidoStatus.CRIADO).build();

        when(repository.findById(id)).thenReturn(Optional.of(pedido));

        service.cancelarPedido(id);

        assertEquals(PedidoStatus.CANCELADO, pedido.getStatus());
        verify(repository).save(pedido);
    }

}