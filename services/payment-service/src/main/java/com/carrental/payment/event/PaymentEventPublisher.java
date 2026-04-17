package com.carrental.payment.event;

import com.carrental.payment.model.Invoice;
import com.carrental.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "car-rental-exchange";
    
    public void publishPaymentCreated(Payment payment) {
        Map<String, Object> event = createPaymentEvent(payment, "payment.created");
        rabbitTemplate.convertAndSend(EXCHANGE, "payment.created", event);
        log.info("Published payment.created event for payment: {}", payment.getPaymentId());
    }
    
    public void publishPaymentCompleted(Payment payment) {
        Map<String, Object> event = createPaymentEvent(payment, "payment.completed");
        event.put("rentalId", payment.getRentalId());
        event.put("customerId", payment.getCustomerId());
        event.put("amount", payment.getAmount());
        event.put("paymentType", payment.getPaymentType());
        rabbitTemplate.convertAndSend(EXCHANGE, "payment.completed", event);
        log.info("Published payment.completed event for payment: {}", payment.getPaymentId());
    }
    
    public void publishPaymentFailed(Payment payment) {
        Map<String, Object> event = createPaymentEvent(payment, "payment.failed");
        rabbitTemplate.convertAndSend(EXCHANGE, "payment.failed", event);
        log.info("Published payment.failed event for payment: {}", payment.getPaymentId());
    }
    
    public void publishRefundIssued(Payment refund) {
        Map<String, Object> event = createPaymentEvent(refund, "payment.refund.issued");
        event.put("rentalId", refund.getRentalId());
        event.put("amount", refund.getAmount());
        rabbitTemplate.convertAndSend(EXCHANGE, "payment.refund.issued", event);
        log.info("Published payment.refund.issued event for refund: {}", refund.getPaymentId());
    }
    
    public void publishInvoiceCreated(Invoice invoice) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "invoice.created");
        event.put("invoiceId", invoice.getInvoiceId());
        event.put("invoiceNumber", invoice.getInvoiceNumber());
        event.put("rentalId", invoice.getRentalId());
        event.put("customerId", invoice.getCustomerId());
        event.put("totalAmount", invoice.getTotalAmount());
        event.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(EXCHANGE, "invoice.created", event);
        log.info("Published invoice.created event for invoice: {}", invoice.getInvoiceNumber());
    }
    
    public void publishInvoicePaid(Invoice invoice) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "invoice.paid");
        event.put("invoiceId", invoice.getInvoiceId());
        event.put("rentalId", invoice.getRentalId());
        event.put("customerId", invoice.getCustomerId());
        event.put("paidAmount", invoice.getPaidAmount());
        event.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(EXCHANGE, "invoice.paid", event);
        log.info("Published invoice.paid event for invoice: {}", invoice.getInvoiceNumber());
    }
    
    private Map<String, Object> createPaymentEvent(Payment payment, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("paymentId", payment.getPaymentId());
        event.put("status", payment.getStatus());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }
}
