package br.com.fiap.pedido.infra.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURACAO DO RABBITMQ
 * Declara exchange, fila e binding para envio do evento PedidoCriado
 */
@Configuration
public class RabbitConfig {

    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String PEDIDO_CRIADO_QUEUE = "pedido.criado";
    public static final String PEDIDO_CRIADO_ROUTING_KEY = "pedido.criado";
    public static final String PEDIDO_ATUALIZADO_QUEUE = "pedido.atualizado";
    public static final String PEDIDO_ATUALIZADO_ROUTING_KEY = "pedido.atualizado";

    /** Declara a exchange do tipo topic para pedidos
     * responsavel por rotear a mensagem para a fila correta */
    @Bean
    public TopicExchange pedidoExchange() {
        return new TopicExchange(PEDIDO_EXCHANGE);
    }

    /** Declara a fila para evento de pedido criado
     * a mensagem fica aguardando para ser consumida */
    @Bean
    public Queue pedidoCriadoQueue() {
        return new Queue(PEDIDO_CRIADO_QUEUE, true); // durable = true
    }

    /** Faz o binding da fila com a exchange usando a routing key
     * chave que define para qual fila a mensagem sera enviada */
    @Bean
    public Binding pedidoCriadoBinding(Queue pedidoCriadoQueue, TopicExchange pedidoExchange) {
        return BindingBuilder.bind(pedidoCriadoQueue).to(pedidoExchange).with(PEDIDO_CRIADO_ROUTING_KEY);
    }

    @Bean
    public Queue pedidoAtualizadoQueue() {
        return new Queue(PEDIDO_ATUALIZADO_QUEUE, true);
    }

    @Bean
    public Binding pedidoAtualizadoBinding(Queue pedidoAtualizadoQueue, TopicExchange pedidoExchange) {
        return BindingBuilder
                .bind(pedidoAtualizadoQueue)
                .to(pedidoExchange)
                .with(PEDIDO_ATUALIZADO_ROUTING_KEY);
    }


}
