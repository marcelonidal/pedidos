package br.com.fiap.pedido.core.domain.model.unit;

import br.com.fiap.pedido.core.domain.model.Pagamento;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PagamentoTest {

    @Test
    void shouldBuildPagamentoWithAllFields() {
        UUID pedidoId = UUID.randomUUID();

        Pagamento pagamento = Pagamento.builder()
                .idPedido(pedidoId)
                .idCartao("1234567812345678")
                .valor(new BigDecimal("150.00"))
                .status(StatusPagamento.APROVADO)
                .dataAprovacao(LocalDateTime.now())
                .build();

        assertEquals(pedidoId, pagamento.getIdPedido());
        assertEquals("1234567812345678", pagamento.getIdCartao());
        assertEquals(new BigDecimal("150.00"), pagamento.getValor());
        assertEquals(StatusPagamento.APROVADO, pagamento.getStatus());
        assertNotNull(pagamento.getDataAprovacao());
    }

}
