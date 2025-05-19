package br.com.fiap.pedido.app.mapper;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.model.ItemPedido;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    default Pedido toModel(PedidoRequestDTO dto) {
        return Pedido.builder()
                .id(null) // gerado pelo banco
                .clienteId(dto.clienteId())
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(calcularTotal(dto))
                .idPagamento(dto.idPagamento())
                .itens(toItens(dto.itens()))
                .build();
    }

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

    default PedidoResponseDTO toResponse(Pedido pedido, PagamentoDTO pagamento) {
        List<ItemPedidoDTO> itens = pedido.getItens().stream()
                .map(item -> new ItemPedidoDTO(
                        item.getProdutoId(),
                        item.getQuantidade(),
                        item.getPrecoUnitario()))
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getDataCriacao(),
                pedido.getStatus().name(),
                pedido.getValorTotal(),
                itens,
                pagamento
        );
    }

    default BigDecimal calcularTotal(PedidoRequestDTO dto) {
        return dto.itens().stream()
                .map(i -> i.precoUnitario().multiply(BigDecimal.valueOf(i.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @AfterMapping
    default void setPedidoNosItens(@MappingTarget Pedido pedido) {
        if (pedido.getItens() != null) {
            pedido.getItens().forEach(item -> item.setPedido(pedido));
        }
    }
}