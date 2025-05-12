package br.com.fiap.pedido.app.mapper;

import br.com.fiap.pedido.app.dto.PedidoRequestDTO;
import br.com.fiap.pedido.app.dto.PedidoResponseDTO;
import br.com.fiap.pedido.core.domain.model.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "CRIADO")
    @Mapping(target = "valorTotal", expression = "java(calcularTotal(dto))")
    Pedido toModel(PedidoRequestDTO dto);

    PedidoResponseDTO toResponse(Pedido model);

    // Método auxiliar default para calcular o total
    default java.math.BigDecimal calcularTotal(PedidoRequestDTO dto) {
        return dto.getItens().stream()
                .map(i -> i.getPrecoUnitario().multiply(java.math.BigDecimal.valueOf(i.getQuantidade())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}