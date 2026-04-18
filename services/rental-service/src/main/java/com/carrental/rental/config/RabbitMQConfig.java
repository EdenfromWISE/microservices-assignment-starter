package com.carrental.rental.config;

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
    public Queue rentalCreatedQueue() {
        return new Queue("rental.created.queue", true);
    }
    
    @Bean
    public Queue rentalConfirmedQueue() {
        return new Queue("rental.confirmed.queue", true);
    }
    
    @Bean
    public Queue rentalCompletedQueue() {
        return new Queue("rental.completed.queue", true);
    }
    
    @Bean
    public Binding rentalCreatedBinding() {
        return BindingBuilder.bind(rentalCreatedQueue())
                .to(carRentalExchange())
                .with("rental.created");
    }
    
    @Bean
    public Binding rentalConfirmedBinding() {
        return BindingBuilder.bind(rentalConfirmedQueue())
                .to(carRentalExchange())
                .with("rental.confirmed");
    }
    
    @Bean
    public Binding rentalCompletedBinding() {
        return BindingBuilder.bind(rentalCompletedQueue())
                .to(carRentalExchange())
                .with("rental.completed");
    }
}
