package br.com.fiap.pedido.app.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String PEDIDO_CRIADO_ROUTING_KEY = "pedido.criado";

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica evento PedidoCriado no RabbitMQ
     * envia para exchange e routing key definidas
     *
     * @param evento objeto com dados do pedido, payload serializado como JSON
     */
    public void publicarPedidoCriado(PedidoCriadoEvent evento) {
        rabbitTemplate.convertAndSend(
                PEDIDO_EXCHANGE,
                PEDIDO_CRIADO_ROUTING_KEY,
                evento
        );
    }

}