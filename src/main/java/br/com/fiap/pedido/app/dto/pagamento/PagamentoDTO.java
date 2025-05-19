package br.com.fiap.pedido.app.dto.pagamento;

import br.com.fiap.pedido.core.domain.model.StatusPagamento;

import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoDTO(
        UUID id,
        StatusPagamento statusPagamento,
        String metodoPagamento,
        LocalDateTime dataPagamento
) {}