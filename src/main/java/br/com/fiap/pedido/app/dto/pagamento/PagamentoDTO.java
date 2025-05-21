package br.com.fiap.pedido.app.dto.pagamento;

import br.com.fiap.pedido.core.domain.model.StatusPagamento;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoDTO(
        UUID idPedido,
        String idCartao,
        BigDecimal valor,
        StatusPagamento status,
        LocalDateTime dataAprovacao
) implements Serializable {}