package com.kr.goukon.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Matching Queue
    public static final String MATCHING_QUEUE = "matching.queue";
    public static final String MATCHING_EXCHANGE = "matching.exchange";
    public static final String MATCHING_ROUTING_KEY = "matching.routing.key";

    // Chat Queue
    public static final String CHAT_QUEUE = "chat.queue";
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_ROUTING_KEY = "chat.routing.key";

    // Matching Queue Configuration
    @Bean
    public Queue matchingQueue() {
        return QueueBuilder.durable(MATCHING_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5분 TTL
                .build();
    }

    @Bean
    public DirectExchange matchingExchange() {
        return new DirectExchange(MATCHING_EXCHANGE);
    }

    @Bean
    public Binding matchingBinding(Queue matchingQueue, DirectExchange matchingExchange) {
        return BindingBuilder.bind(matchingQueue).to(matchingExchange).with(MATCHING_ROUTING_KEY);
    }

    // Chat Queue Configuration
    @Bean
    public Queue chatQueue() {
        return QueueBuilder.durable(CHAT_QUEUE).build();
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, DirectExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange).with(CHAT_ROUTING_KEY);
    }

    // Message Converter (JSON)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
