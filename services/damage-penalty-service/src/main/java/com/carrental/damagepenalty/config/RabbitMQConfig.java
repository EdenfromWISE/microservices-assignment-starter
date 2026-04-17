package com.carrental.damagepenalty.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "car-rental-exchange";
    
    @Bean
    public TopicExchange carRentalExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }
    
    @Bean
    public Queue damageReportedQueue() {
        return new Queue("damage.reported.queue", true);
    }
    
    @Bean
    public Queue damageUpdatedQueue() {
        return new Queue("damage.updated.queue", true);
    }
    
    @Bean
    public Queue penaltyCreatedQueue() {
        return new Queue("penalty.created.queue", true);
    }
    
    @Bean
    public Queue penaltyPaidQueue() {
        return new Queue("penalty.paid.queue", true);
    }
    
    @Bean
    public Binding damageReportedBinding() {
        return BindingBuilder.bind(damageReportedQueue())
            .to(carRentalExchange())
            .with("damage.reported");
    }
    
    @Bean
    public Binding damageUpdatedBinding() {
        return BindingBuilder.bind(damageUpdatedQueue())
            .to(carRentalExchange())
            .with("damage.updated");
    }
    
    @Bean
    public Binding penaltyCreatedBinding() {
        return BindingBuilder.bind(penaltyCreatedQueue())
            .to(carRentalExchange())
            .with("penalty.created");
    }
    
    @Bean
    public Binding penaltyPaidBinding() {
        return BindingBuilder.bind(penaltyPaidQueue())
            .to(carRentalExchange())
            .with("penalty.paid");
    }
}
