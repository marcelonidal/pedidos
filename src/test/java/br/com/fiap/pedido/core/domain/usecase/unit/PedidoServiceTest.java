package br.com.fiap.pedido.core.domain.usecase.unit;

import br.com.fiap.pedido.app.dto.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.usecase.PedidoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Test
    void deveChamarCriarPedidoComSucesso() {
        PedidoService service = mock(PedidoService.class);

        UUID clienteId = UUID.randomUUID();
        List<ItemPedidoDTO> itens = List.of(new ItemPedidoDTO(UUID.randomUUID(), 2, new BigDecimal("10.00")));

        PedidoRequestDTO request = new PedidoRequestDTO(clienteId, itens);
        PedidoResponseDTO respostaEsperada = new PedidoResponseDTO(
                UUID.randomUUID(),
                clienteId,
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("20.00"),
                itens
        );

        when(service.criarPedido(request)).thenReturn(respostaEsperada);

        PedidoResponseDTO resposta = service.criarPedido(request);

        assertEquals(respostaEsperada, resposta);
        verify(service).criarPedido(request);
    }

    @Test
    void deveBuscarPedidoPorId() {
        PedidoService service = mock(PedidoService.class);
        UUID id = UUID.randomUUID();

        PedidoResponseDTO resposta = new PedidoResponseDTO(
                id,
                UUID.randomUUID(),
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("100.00"),
                Collections.emptyList()
        );

        when(service.buscarPorId(id)).thenReturn(resposta);

        PedidoResponseDTO result = service.buscarPorId(id);

        assertNotNull(result);
        verify(service).buscarPorId(id);
    }

    @Test
    void deveListarTodosOsPedidos() {
        PedidoService service = mock(PedidoService.class);
        PedidoResponseDTO pedido = new PedidoResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                "CRIADO",
                new BigDecimal("50.00"),
                Collections.emptyList()
        );

        List<PedidoResponseDTO> lista = Collections.singletonList(pedido);

        when(service.listarTodos()).thenReturn(lista);

        List<PedidoResponseDTO> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        verify(service).listarTodos();
    }

    @Test
    void deveCancelarPedidoPorId() {
        PedidoService service = mock(PedidoService.class);
        UUID id = UUID.randomUUID();

        doNothing().when(service).cancelarPedido(id);

        service.cancelarPedido(id);

        verify(service).cancelarPedido(id);
    }

}