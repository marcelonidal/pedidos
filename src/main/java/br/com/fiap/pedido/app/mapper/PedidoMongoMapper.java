package br.com.fiap.pedido.app.mapper;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.core.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PedidoMongoMapper {

    public PedidoMongo toPedidoMongo(PedidoResponseMongoDTO dto) {
        return PedidoMongo.builder()
                .id(UUID.randomUUID().toString())
                .idPedido(dto.id())
                .clienteCpf(dto.clienteCpf())
                .dataCriacao(dto.dataCriacao())
                .status(PedidoStatus.valueOf(dto.status()))
                .valorTotal(dto.valorTotal())
                .itens(toItens(dto.itens()))
                .pagamento(toPagamento(dto.pagamento()))
                .build();
    }

    public PedidoResponseMongoDTO toResponse(Pedido pedido, PagamentoDTO pagamento) {
        return new PedidoResponseMongoDTO(
                pedido.getId(),
                pedido.getClienteCpf(),
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
    public ItemPedido toItem(ItemPedidoDTO dto) {
        return ItemPedido.builder()
                .id(null)
                .produtoId(dto.produtoId())
                .quantidade(dto.quantidade())
                .precoUnitario(dto.precoUnitario())
                .pedido(null)
                .build();
    }

    public List<ItemPedido> toItens(List<ItemPedidoDTO> dtoList) {
        if (dtoList == null) return new ArrayList<>();
        return dtoList.stream()
                .map(this::toItem)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // de entidade -> DTO
    public ItemPedidoDTO toItemDTO(ItemPedido item) {
        return new ItemPedidoDTO(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario());
    }

    public Pagamento toPagamento(PagamentoDTO dto) {
        if (dto == null) return null;

        return Pagamento.builder()
                .idPedido(dto.idPedido())
                .idCartao(dto.idCartao())
                .valor(dto.valor())
                .status(dto.status())
                .dataAprovacao(dto.dataAprovacao())
                .build();
    }

}