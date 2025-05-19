package br.com.fiap.pedido.app.event;

import br.com.fiap.pedido.app.dto.pedido.PedidoResponseDTO;
import br.com.fiap.pedido.app.dto.pedido.PedidoResponseMongoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String PEDIDO_CRIADO_ROUTING_KEY = "pedido.criado";
    public static final String PEDIDO_ATUALIZADO_ROUTING_KEY = "pedido.atualizado";

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica evento PedidoCriado no RabbitMQ
     * Envia para exchange e routing key definidas
     *
     * @param dto objeto PedidoResponseDTO com dados do pedido (payload serializado como JSON)
     */
    public void publicarPedidoCriado(PedidoResponseDTO dto) {

        log.info("Publicando evento de pedido criado para RabbitMQ: {}", dto.id());

        rabbitTemplate.convertAndSend(
                PEDIDO_EXCHANGE,
                PEDIDO_CRIADO_ROUTING_KEY,
                dto
        );
    }

    public void publicarAtualizacaoPedido(PedidoResponseMongoDTO dto) {
        log.info("Publicando evento de atualização de pedido para o MongoDB: {}", dto.id());

        rabbitTemplate.convertAndSend(
                PEDIDO_EXCHANGE,
                PEDIDO_ATUALIZADO_ROUTING_KEY,
                dto
        );
    }

}