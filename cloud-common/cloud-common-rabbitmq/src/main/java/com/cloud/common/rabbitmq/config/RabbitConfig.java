package com.cloud.common.rabbitmq.config;

import com.cloud.common.rabbitmq.constant.RabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 自动配置：JSON 消息转换器、业务拓扑以及死信队列。
 * {@link MessageConverter}  bean 会由 Spring Boot 自动应用到其 RabbitTemplate。
 */
@AutoConfiguration
@ConditionalOnClass(MessageConverter.class)
public class RabbitConfig {

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter jacksonMessageConverter() {
        // Spring AMQP 4 基于 Jackson 3，使用 JacksonJsonMessageConverter（替代已废弃的 Jackson2JsonMessageConverter）
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange businessExchange() {
        return new TopicExchange(RabbitConstants.BUSINESS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(RabbitConstants.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue businessQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitConstants.DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RabbitConstants.DLX_ROUTING_KEY);
        return QueueBuilder.durable(RabbitConstants.BUSINESS_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(RabbitConstants.DLX_QUEUE).build();
    }

    @Bean
    public Binding businessBinding() {
        return BindingBuilder.bind(businessQueue()).to(businessExchange()).with(RabbitConstants.BUSINESS_ROUTING_KEY);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(RabbitConstants.DLX_ROUTING_KEY);
    }
}
