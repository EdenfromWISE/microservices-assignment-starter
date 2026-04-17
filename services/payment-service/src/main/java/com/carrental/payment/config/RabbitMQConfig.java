package com.carrental.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE = "car-rental-exchange";
    
    @Bean
    public TopicExchange carRentalExchange() {
        return new TopicExchange(EXCHANGE);
    }
    
    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue("payment.completed.queue", true);
    }
    
    @Bean
    public Queue invoiceCreatedQueue() {
        return new Queue("invoice.created.queue", true);
    }
    
    @Bean
    public Queue invoicePaidQueue() {
        return new Queue("invoice.paid.queue", true);
    }
    
    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedQueue())
                .to(carRentalExchange())
                .with("payment.completed");
    }
    
    @Bean
    public Binding invoiceCreatedBinding() {
        return BindingBuilder.bind(invoiceCreatedQueue())
                .to(carRentalExchange())
                .with("invoice.created");
    }
    
    @Bean
    public Binding invoicePaidBinding() {
        return BindingBuilder.bind(invoicePaidQueue())
                .to(carRentalExchange())
                .with("invoice.paid");
    }
}
