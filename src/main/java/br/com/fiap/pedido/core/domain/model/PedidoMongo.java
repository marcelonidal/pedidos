package br.com.fiap.pedido.core.domain.model;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Document(collection = "pedidos")
public class PedidoMongo {

    @Id
    private String id;

    private UUID idPedido;
    private String clienteCpf;
    private LocalDateTime dataCriacao;
    private PedidoStatus status;
    private BigDecimal valorTotal;
    private List<ItemPedido> itens;
    private Pagamento pagamento;
}