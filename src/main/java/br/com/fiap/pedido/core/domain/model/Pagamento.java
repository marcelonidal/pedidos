package br.com.fiap.pedido.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    private UUID idPedido;
    private String idCartao;
    private BigDecimal valor;
    private StatusPagamento status;
    private LocalDateTime dataAprovacao;
}
