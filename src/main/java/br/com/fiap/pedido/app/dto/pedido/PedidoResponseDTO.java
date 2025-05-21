package br.com.fiap.pedido.app.dto.pedido;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        String clienteCpf,
        LocalDateTime dataCriacao,
        String status,
        BigDecimal valorTotal,
        List<ItemPedidoDTO> itens,
        PagamentoDTO pagamento
) implements Serializable {}