package br.com.fiap.pedido.app.mapper;

import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.model.Pedido;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "CRIADO")
    @Mapping(target = "valorTotal", expression = "java(calcularTotal(dto))")
    Pedido toModel(PedidoRequestDTO dto);

    PedidoResponseDTO toResponse(Pedido model);

    default java.math.BigDecimal calcularTotal(PedidoRequestDTO dto) {
        return dto.itens().stream()
                .map(i -> i.precoUnitario().multiply(java.math.BigDecimal.valueOf(i.quantidade())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Depois que o MapStruct criar o Pedido, associamos o 'pedido' em cada item
     */
    @AfterMapping
    default void setPedidoNosItens(@MappingTarget Pedido pedido) {
        if (pedido.getItens() != null) {
            pedido.getItens().forEach(item -> item.setPedido(pedido));
        }
    }

}