package com.carrental.damagepenalty.event;

import com.carrental.damagepenalty.model.Penalty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PenaltyEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE = "car-rental-exchange";
    
    public void publishPenaltyCreated(Penalty penalty) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "penalty.created");
        event.put("penaltyId", penalty.getPenaltyId().toString());
        event.put("customerId", penalty.getCustomerId().toString());
        event.put("amount", penalty.getPenaltyAmount());
        
        rabbitTemplate.convertAndSend(EXCHANGE, "penalty.created", event);
        log.info("Published penalty.created event for penalty: {}", penalty.getPenaltyId());
    }
    
    public void publishPenaltyPaid(Penalty penalty, BigDecimal amount) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "penalty.paid");
        event.put("penaltyId", penalty.getPenaltyId().toString());
        event.put("customerId", penalty.getCustomerId().toString());
        event.put("amount", amount);
        event.put("status", penalty.getPaymentStatus().name());
        
        rabbitTemplate.convertAndSend(EXCHANGE, "penalty.paid", event);
        log.info("Published penalty.paid event for penalty: {}", penalty.getPenaltyId());
    }
}
