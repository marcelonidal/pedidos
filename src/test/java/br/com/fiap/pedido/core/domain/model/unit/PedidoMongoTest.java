package br.com.fiap.pedido.core.domain.model.unit;

import br.com.fiap.pedido.core.domain.model.Pagamento;
import br.com.fiap.pedido.core.domain.model.PedidoMongo;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PedidoMongoTest {

    @Test
    void shouldBuildPedidoMongoWithAllFields() {
        UUID idPedido = UUID.randomUUID();
        Pagamento pagamento = Pagamento.builder()
                .idPedido(idPedido)
                .idCartao("9999888877776666")
                .valor(new BigDecimal("99.90"))
                .status(StatusPagamento.AGUARDANDO)
                .dataAprovacao(LocalDateTime.now())
                .build();

        PedidoMongo mongo = PedidoMongo.builder()
                .id("mongo-id-123")
                .idPedido(idPedido)
                .clienteCpf("98765432100")
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.PAGO)
                .valorTotal(new BigDecimal("99.90"))
                .itens(Collections.emptyList())
                .pagamento(pagamento)
                .build();

        assertEquals("mongo-id-123", mongo.getId());
        assertEquals(idPedido, mongo.getIdPedido());
        assertEquals("98765432100", mongo.getClienteCpf());
        assertEquals(PedidoStatus.PAGO, mongo.getStatus());
        assertEquals(new BigDecimal("99.90"), mongo.getValorTotal());
        assertEquals(pagamento, mongo.getPagamento());
    }

}
