package br.com.fiap.pedido.app.mapper.unit;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.mapper.PedidoMongoMapper;
import br.com.fiap.pedido.core.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoMongoMapperTest {

    private final PedidoMongoMapper mapper = new PedidoMongoMapper();

    @Test
    void shouldConvertToPedidoMongo() {
        UUID pedidoId = UUID.randomUUID();
        String cpf = "12345678900";

        List<ItemPedidoDTO> itens = List.of(
                new ItemPedidoDTO(UUID.randomUUID(), 2, new BigDecimal("50.00"))
        );

        PagamentoDTO pagamentoDTO = new PagamentoDTO(
                pedidoId,
                "1111222233334444",
                new BigDecimal("100.00"),
                StatusPagamento.APROVADO,
                LocalDateTime.now()
        );

        PedidoResponseMongoDTO dto = new PedidoResponseMongoDTO(
                pedidoId,
                cpf,
                LocalDateTime.now(),
                "PAGO",
                new BigDecimal("100.00"),
                itens,
                pagamentoDTO
        );

        PedidoMongo mongo = mapper.toPedidoMongo(dto);

        assertNotNull(mongo);
        assertEquals(pedidoId, mongo.getIdPedido());
        assertEquals(cpf, mongo.getClienteCpf());
        assertEquals(dto.status(), mongo.getStatus().name());
        assertEquals(1, mongo.getItens().size());
        assertEquals(pagamentoDTO.status(), mongo.getPagamento().getStatus());
    }

    @Test
    void shouldConvertToResponseDTO() {
        UUID pedidoId = UUID.randomUUID();
        String cpf = "98765432100";

        List<ItemPedido> itens = List.of(
                ItemPedido.builder()
                        .produtoId(UUID.randomUUID())
                        .quantidade(1)
                        .precoUnitario(new BigDecimal("10.00"))
                        .build()
        );

        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .clienteCpf(cpf)
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(new BigDecimal("10.00"))
                .itens(itens)
                .build();

        PagamentoDTO pagamentoDTO = new PagamentoDTO(
                pedidoId,
                "9999888877776666",
                new BigDecimal("10.00"),
                StatusPagamento.AGUARDANDO,
                null
        );

        PedidoResponseMongoDTO dto = mapper.toResponse(pedido, pagamentoDTO);

        assertNotNull(dto);
        assertEquals(pedidoId, dto.id());
        assertEquals("CRIADO", dto.status());
        assertEquals(1, dto.itens().size());
        assertEquals(StatusPagamento.AGUARDANDO, dto.pagamento().status());
    }

    @Test
    void shouldConvertItemDTOToItemModel() {
        UUID produtoId = UUID.randomUUID();
        ItemPedidoDTO dto = new ItemPedidoDTO(produtoId, 3, new BigDecimal("15.00"));

        ItemPedido item = mapper.toItem(dto);

        assertNotNull(item);
        assertEquals(produtoId, item.getProdutoId());
        assertEquals(3, item.getQuantidade());
        assertEquals(new BigDecimal("15.00"), item.getPrecoUnitario());
    }

    @Test
    void shouldConvertItemToItemDTO() {
        UUID produtoId = UUID.randomUUID();
        ItemPedido item = ItemPedido.builder()
                .produtoId(produtoId)
                .quantidade(5)
                .precoUnitario(new BigDecimal("12.00"))
                .build();

        ItemPedidoDTO dto = mapper.toItemDTO(item);

        assertNotNull(dto);
        assertEquals(produtoId, dto.produtoId());
        assertEquals(5, dto.quantidade());
        assertEquals(new BigDecimal("12.00"), dto.precoUnitario());
    }

    @Test
    void shouldReturnEmptyListIfItemListIsNull() {
        List<ItemPedido> result = mapper.toItens(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldConvertPagamentoDTOToModel() {
        UUID pedidoId = UUID.randomUUID();
        PagamentoDTO dto = new PagamentoDTO(
                pedidoId,
                "1111222233334444",
                new BigDecimal("80.00"),
                StatusPagamento.APROVADO,
                LocalDateTime.now()
        );

        Pagamento pagamento = mapper.toPagamento(dto);

        assertNotNull(pagamento);
        assertEquals(pedidoId, pagamento.getIdPedido());
        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
    }

    @Test
    void shouldReturnNullWhenPagamentoDTOIsNull() {
        Pagamento pagamento = mapper.toPagamento(null);
        assertNull(pagamento);
    }

}
