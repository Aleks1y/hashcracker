package com.example.manager.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2XmlMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2XmlMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setExchange("directExchange");
        return rabbitTemplate;
    }

    @Bean
    public Queue workerQueue() {
        return new Queue("to_worker_queue");
    }

    @Bean
    public Queue managerQueue() {
        return new Queue("to_manager_queue");
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange("directExchange", true, false);
    }

    @Bean
    Binding bindWorkerQueue() {
        return BindingBuilder.bind(workerQueue()).to(exchange()).with("to_worker");
    }

}