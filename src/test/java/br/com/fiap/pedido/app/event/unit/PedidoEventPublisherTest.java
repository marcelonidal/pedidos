package br.com.fiap.pedido.app.event.unit;

import br.com.fiap.pedido.app.dto.pagamento.PagamentoDTO;
import br.com.fiap.pedido.app.dto.pedido.ItemPedidoDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import br.com.fiap.pedido.app.event.PedidoEventPublisher;
import br.com.fiap.pedido.core.domain.model.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PedidoEventPublisherTest {

    private RabbitTemplate rabbitTemplate;
    private PedidoEventPublisher publisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        publisher = new PedidoEventPublisher(rabbitTemplate);
    }

    @Test
    void shouldPublishPedidoCriadoEvent() {
        UUID pedidoId = UUID.randomUUID();

        PedidoResponseDTO dto = new PedidoResponseDTO(
                pedidoId,
                "12345678901",
                LocalDateTime.now(),
                "CRIADO",
                BigDecimal.valueOf(100),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 2, BigDecimal.TEN)),
                new PagamentoDTO(pedidoId, "cartao", BigDecimal.TEN, StatusPagamento.AGUARDANDO, null)
        );

        publisher.publicarPedidoCriado(dto);

        verify(rabbitTemplate).convertAndSend(
                eq(PedidoEventPublisher.PEDIDO_EXCHANGE),
                eq(PedidoEventPublisher.PEDIDO_CRIADO_ROUTING_KEY),
                eq(dto)
        );
    }

    @Test
    void shouldPublishAtualizacaoPedidoEvent() {
        UUID pedidoId = UUID.randomUUID();

        PedidoResponseMongoDTO dto = new PedidoResponseMongoDTO(
                pedidoId,
                "12345678901",
                LocalDateTime.now(),
                "PAGO",
                BigDecimal.valueOf(120),
                List.of(new ItemPedidoDTO(UUID.randomUUID(), 1, BigDecimal.valueOf(120))),
                new PagamentoDTO(pedidoId, "456", BigDecimal.valueOf(120), StatusPagamento.APROVADO, LocalDateTime.now())
        );

        publisher.publicarAtualizacaoPedido(dto);

        verify(rabbitTemplate).convertAndSend(
                eq(PedidoEventPublisher.PEDIDO_EXCHANGE),
                eq(PedidoEventPublisher.PEDIDO_ATUALIZADO_ROUTING_KEY),
                eq(dto)
        );
    }

}
