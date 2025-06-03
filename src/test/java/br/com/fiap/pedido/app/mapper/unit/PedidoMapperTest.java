package br.com.fiap.pedido.app.mapper.unit;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.mapper.PedidoMapper;
import br.com.fiap.pedido.core.domain.model.ItemPedido;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoMapperTest {

    private final PedidoMapper mapper = new PedidoMapper();

    @Test
    void shouldMapRequestDTOToPedidoModel() {
        UUID produtoId = UUID.randomUUID();
        List<ItemPedidoDTO> itens = List.of(new ItemPedidoDTO(produtoId, 2, new BigDecimal("10.00")));

        PedidoRequestDTO dto = new PedidoRequestDTO("12345678900", itens, "1234123412341234");

        Pedido pedido = mapper.toModel(dto);

        assertNotNull(pedido);
        assertEquals("12345678900", pedido.getClienteCpf());
        assertEquals(PedidoStatus.CRIADO, pedido.getStatus());
        assertEquals(new BigDecimal("20.00"), pedido.getValorTotal());
        assertEquals(1, pedido.getItens().size());
    }

    @Test
    void shouldMapPedidoToResponseDTO() {
        UUID pedidoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        ItemPedido item = ItemPedido.builder()
                .id(null)
                .produtoId(produtoId)
                .quantidade(2)
                .precoUnitario(new BigDecimal("50.00"))
                .build();

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteCpf("98765432100")
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(new BigDecimal("100.00"))
                .idCartao("1234567812345678")
                .itens(List.of(item))
                .build();

        PagamentoDTO pagamento = new PagamentoDTO(
                pedidoId,
                "1234567812345678",
                new BigDecimal("100.00"),
                StatusPagamento.AGUARDANDO,
                null
        );

        PedidoResponseDTO dto = mapper.toResponse(pedido, pagamento);

        assertNotNull(dto);
        assertEquals(pedidoId, dto.id());
        assertEquals("CRIADO", dto.status());
        assertEquals(new BigDecimal("100.00"), dto.valorTotal());
        assertEquals(1, dto.itens().size());
    }

    @Test
    void shouldMapItemPedidoDTOToItemPedidoModel() {
        UUID produtoId = UUID.randomUUID();
        ItemPedidoDTO dto = new ItemPedidoDTO(produtoId, 3, new BigDecimal("30.00"));

        ItemPedido item = mapper.toItem(dto);

        assertNotNull(item);
        assertEquals(produtoId, item.getProdutoId());
        assertEquals(3, item.getQuantidade());
        assertEquals(new BigDecimal("30.00"), item.getPrecoUnitario());
    }

    @Test
    void shouldCalculateTotalFromRequestDTO() {
        List<ItemPedidoDTO> itens = List.of(
                new ItemPedidoDTO(UUID.randomUUID(), 1, new BigDecimal("10")),
                new ItemPedidoDTO(UUID.randomUUID(), 2, new BigDecimal("15"))
        );

        PedidoRequestDTO dto = new PedidoRequestDTO("99999999999", itens, "4444333322221111");

        BigDecimal total = mapper.calcularTotal(dto);

        assertEquals(new BigDecimal("40"), total);
    }

    @Test
    void shouldReturnEmptyItemListWhenInputIsNull() {
        List<ItemPedido> itens = mapper.toItens(null);
        assertNotNull(itens);
        assertTrue(itens.isEmpty());
    }

}
