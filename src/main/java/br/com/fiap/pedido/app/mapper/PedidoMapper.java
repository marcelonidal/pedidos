package br.com.fiap.pedido.app.mapper;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.model.ItemPedido;
import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    public Pedido toModel(PedidoRequestDTO dto) {
        Pedido pedido = Pedido.builder()
                .id(null)
                .clienteId(dto.clienteId())
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(calcularTotal(dto))
                .idPagamento(dto.idPagamento())
                .itens(toItens(dto.itens()))
                .build();

        if (pedido.getItens() != null) {
            pedido.getItens().forEach(item -> item.setPedido(pedido));
        }

        return pedido;
    }

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

    public PedidoResponseDTO toResponse(Pedido pedido, PagamentoDTO pagamento) {
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

    public BigDecimal calcularTotal(PedidoRequestDTO dto) {
        return dto.itens().stream()
                .map(i -> i.precoUnitario().multiply(BigDecimal.valueOf(i.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}