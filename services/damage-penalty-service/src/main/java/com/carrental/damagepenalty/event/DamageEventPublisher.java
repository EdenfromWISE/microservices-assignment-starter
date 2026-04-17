package com.carrental.damagepenalty.event;

import com.carrental.damagepenalty.model.DamageReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DamageEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE = "car-rental-exchange";
    
    public void publishDamageReported(DamageReport damage) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "damage.reported");
        event.put("damageId", damage.getDamageId().toString());
        event.put("vehicleId", damage.getVehicleId().toString());
        event.put("rentalId", damage.getRentalId().toString());
        event.put("severity", damage.getSeverity().name());
        event.put("repairCost", damage.getRepairCost());
        
        rabbitTemplate.convertAndSend(EXCHANGE, "damage.reported", event);
        log.info("Published damage.reported event for damage: {}", damage.getDamageId());
    }
    
    public void publishDamageUpdated(DamageReport damage) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "damage.updated");
        event.put("damageId", damage.getDamageId().toString());
        event.put("status", damage.getStatus().name());
        
        rabbitTemplate.convertAndSend(EXCHANGE, "damage.updated", event);
        log.info("Published damage.updated event for damage: {}", damage.getDamageId());
    }
}
