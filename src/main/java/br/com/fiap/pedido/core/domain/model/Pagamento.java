package br.com.fiap.pedido.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    private UUID id;
    private UUID pedidoId;
    private StatusPagamento statusPagamento;
    private String metodoPagamento;
    private LocalDateTime dataPagamento;
}
