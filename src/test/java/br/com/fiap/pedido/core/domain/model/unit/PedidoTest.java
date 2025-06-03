package br.com.fiap.pedido.core.domain.model.unit;

import br.com.fiap.pedido.core.domain.model.Pedido;
import br.com.fiap.pedido.core.domain.model.PedidoStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PedidoTest {

    @Test
    void shouldCreatePedidoWithFields() {
        UUID id = UUID.randomUUID();
        String cpf = "12345678900";
        String cartao = "1234123412341234";

        Pedido pedido = Pedido.builder()
                .id(id)
                .clienteCpf(cpf)
                .dataCriacao(LocalDateTime.now())
                .status(PedidoStatus.CRIADO)
                .valorTotal(BigDecimal.TEN)
                .idCartao(cartao)
                .itens(Collections.emptyList())
                .build();

        assertEquals(id, pedido.getId());
        assertEquals(cpf, pedido.getClienteCpf());
        assertEquals(PedidoStatus.CRIADO, pedido.getStatus());
        assertEquals(BigDecimal.TEN, pedido.getValorTotal());
        assertEquals(cartao, pedido.getIdCartao());
        assertTrue(pedido.getItens().isEmpty());
    }

    @Test
    void shouldChangeStatusToCancelado() {
        Pedido pedido = new Pedido();
        pedido.setStatus(PedidoStatus.CRIADO);

        pedido.setStatus(PedidoStatus.CANCELADO);

        assertEquals(PedidoStatus.CANCELADO, pedido.getStatus());
    }

}
