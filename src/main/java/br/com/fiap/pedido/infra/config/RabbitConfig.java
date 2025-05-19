package br.com.fiap.pedido.infra.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * CONFIGURACAO DO RABBITMQ
 * Declara exchange, fila e binding para envio do evento PedidoCriado
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    public static final String PEDIDO_EXCHANGE = "pedido.exchange";
    public static final String PEDIDO_CRIADO_QUEUE = "pedido.criado";
    public static final String PEDIDO_CRIADO_ROUTING_KEY = "pedido.criado";
    public static final String PEDIDO_ATUALIZADO_QUEUE = "pedido.atualizado";
    public static final String PEDIDO_ATUALIZADO_ROUTING_KEY = "pedido.atualizado";

    /** Exchange **/
    @Bean
    public TopicExchange pedidoExchange() {
        return new TopicExchange(PEDIDO_EXCHANGE);
    }

    /** Filas **/
    @Bean
    public Queue pedidoCriadoQueue() {
        return new Queue(PEDIDO_CRIADO_QUEUE, true);
    }

    @Bean
    public Queue pedidoAtualizadoQueue() {
        return new Queue(PEDIDO_ATUALIZADO_QUEUE, true);
    }

    /** Bindings **/
    @Bean
    public Binding pedidoCriadoBinding(Queue pedidoCriadoQueue, TopicExchange pedidoExchange) {
        return BindingBuilder.bind(pedidoCriadoQueue).to(pedidoExchange).with(PEDIDO_CRIADO_ROUTING_KEY);
    }

    @Bean
    public Binding pedidoAtualizadoBinding(Queue pedidoAtualizadoQueue, TopicExchange pedidoExchange) {
        return BindingBuilder.bind(pedidoAtualizadoQueue).to(pedidoExchange).with(PEDIDO_ATUALIZADO_ROUTING_KEY);
    }

    /** Conversor de mensagens para JSON **/
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** Template para publicar mensagens com JSON **/
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /** Factory que garante que os listeners entendam JSON **/
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

}