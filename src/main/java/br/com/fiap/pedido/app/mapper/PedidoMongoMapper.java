package br.com.fiap.pedido.app.mapper;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.core.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PedidoMongoMapper {

    default PedidoMongo toPedidoMongo(PedidoResponseMongoDTO dto) {
        return PedidoMongo.builder()
                .id(UUID.randomUUID().toString())
                .idPedido(dto.id())
                .idCliente(dto.clienteId())
                .dataCriacao(dto.dataCriacao())
                .status(PedidoStatus.valueOf(dto.status()))
                .valorTotal(dto.valorTotal())
                .itens(toItens(dto.itens()))
                .pagamento(toPagamento(dto.pagamento()))
                .build();
    }

    default PedidoResponseMongoDTO toResponse(Pedido pedido, PagamentoDTO pagamento) {
        return new PedidoResponseMongoDTO(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getDataCriacao(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                pedido.getItens().stream()
                        .map(this::toItemDTO)
                        .toList(),
                pagamento
        );
    }

    // de DTO -> entidade
    default ItemPedido toItem(ItemPedidoDTO dto) {
        return ItemPedido.builder()
                .id(null)
                .produtoId(dto.produtoId())
                .quantidade(dto.quantidade())
                .precoUnitario(dto.precoUnitario())
                .pedido(null)
                .build();
    }

    default List<ItemPedido> toItens(List<ItemPedidoDTO> dtoList) {
        if (dtoList == null) return List.of();
        return dtoList.stream()
                .map(this::toItem)
                .toList();
    }

    // de entidade -> DTO
    default ItemPedidoDTO toItemDTO(ItemPedido item) {
        return new ItemPedidoDTO(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario());
    }

    default Pagamento toPagamento(PagamentoDTO dto) {
        if (dto == null) return null;

        return Pagamento.builder()
                .id(dto.id())
                .statusPagamento(dto.statusPagamento())
                .metodoPagamento(dto.metodoPagamento())
                .dataPagamento(dto.dataPagamento())
                .build();
    }

}