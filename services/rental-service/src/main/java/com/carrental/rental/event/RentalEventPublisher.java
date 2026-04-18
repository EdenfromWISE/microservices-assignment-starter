package com.carrental.rental.event;

import com.carrental.rental.model.Rental;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RentalEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "car-rental-exchange";
    
    public void publishRentalCreated(Rental rental) {
        Map<String, Object> event = createEventData(rental, "rental.created");
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.created", event);
        log.info("Published rental.created event for rental: {}", rental.getRentalId());
    }
    
    public void publishRentalConfirmed(Rental rental) {
        Map<String, Object> event = createEventData(rental, "rental.confirmed");
        event.put("vehicleId", rental.getVehicleId());
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.confirmed", event);
        log.info("Published rental.confirmed event for rental: {}", rental.getRentalId());
    }
    
    public void publishVehiclePickedUp(Rental rental) {
        Map<String, Object> event = createEventData(rental, "rental.pickup");
        event.put("vehicleId", rental.getVehicleId());
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.pickup", event);
        log.info("Published rental.pickup event for rental: {}", rental.getRentalId());
    }
    
    public void publishVehicleReturned(Rental rental) {
        Map<String, Object> event = createEventData(rental, "rental.return");
        event.put("vehicleId", rental.getVehicleId());
        event.put("actualReturnDate", rental.getActualReturnDate());
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.return", event);
        log.info("Published rental.return event for rental: {}", rental.getRentalId());
    }
    
    public void publishInspectionCompleted(Rental rental, boolean hasDamage) {
        Map<String, Object> event = createEventData(rental, "rental.inspection.completed");
        event.put("vehicleId", rental.getVehicleId());
        event.put("hasDamage", hasDamage);
        event.put("damageReportId", rental.getDamageReportId());
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.inspection.completed", event);
        log.info("Published rental.inspection.completed event for rental: {}", rental.getRentalId());
    }
    
    public void publishRentalCompleted(Rental rental) {
        Map<String, Object> event = createEventData(rental, "rental.completed");
        event.put("vehicleId", rental.getVehicleId());
        event.put("customerId", rental.getCustomerId());
        event.put("totalCost", rental.getTotalCost());
        event.put("penaltyAmount", rental.getPenaltyAmount());
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.completed", event);
        log.info("Published rental.completed event for rental: {}", rental.getRentalId());
    }
    
    public void publishRentalCancelled(Rental rental) {
        Map<String, Object> event = createEventData(rental, "rental.cancelled");
        event.put("vehicleId", rental.getVehicleId());
        rabbitTemplate.convertAndSend(EXCHANGE, "rental.cancelled", event);
        log.info("Published rental.cancelled event for rental: {}", rental.getRentalId());
    }
    
    private Map<String, Object> createEventData(Rental rental, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("rentalId", rental.getRentalId());
        event.put("status", rental.getStatus());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }
}
