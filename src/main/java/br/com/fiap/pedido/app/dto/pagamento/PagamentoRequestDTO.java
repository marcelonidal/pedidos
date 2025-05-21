package br.com.fiap.pedido.app.dto.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoRequestDTO(
        UUID idPedido,
        String idCartao,
        BigDecimal valor
) {}