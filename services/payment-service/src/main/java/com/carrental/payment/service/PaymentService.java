package com.carrental.payment.service;

import com.carrental.payment.dto.PaymentCreateDTO;
import com.carrental.payment.dto.PaymentProcessDTO;
import com.carrental.payment.event.PaymentEventPublisher;
import com.carrental.payment.exception.PaymentNotFoundException;
import com.carrental.payment.model.Payment;
import com.carrental.payment.model.PaymentStatus;
import com.carrental.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    
    @Transactional
    public Payment createPayment(PaymentCreateDTO dto) {
        log.info("Creating payment for customer: {}", dto.getCustomerId());
        
        Payment payment = Payment.builder()
                .rentalId(dto.getRentalId())
                .customerId(dto.getCustomerId())
                .invoiceId(dto.getInvoiceId())
                .paymentType(dto.getPaymentType())
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .description(dto.getDescription())
                .notes(dto.getNotes())
                .status(PaymentStatus.PENDING)
                .build();
        
        Payment saved = paymentRepository.save(payment);
        eventPublisher.publishPaymentCreated(saved);
        
        log.info("Payment created successfully: {}", saved.getPaymentId());
        return saved;
    }
    
    @Transactional
    public Payment processPayment(String paymentId, PaymentProcessDTO dto) {
        log.info("Processing payment: {}", paymentId);
        Payment payment = getPaymentById(paymentId);
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING status");
        }
        
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        
        // Simulate payment processing
        boolean success = processWithPaymentGateway(payment, dto.getTransactionReference());
        
        if (success) {
            payment.markAsCompleted(dto.getTransactionReference());
            eventPublisher.publishPaymentCompleted(payment);
            log.info("Payment processed successfully: {}", paymentId);
        } else {
            payment.markAsFailed("Payment gateway declined");
            eventPublisher.publishPaymentFailed(payment);
            log.error("Payment processing failed: {}", paymentId);
        }
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment createRefund(String originalPaymentId, BigDecimal refundAmount, String reason) {
        log.info("Creating refund for payment: {}, amount: {}", originalPaymentId, refundAmount);
        
        Payment originalPayment = getPaymentById(originalPaymentId);
        
        if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Original payment must be COMPLETED");
        }
        
        Payment refund = Payment.builder()
                .rentalId(originalPayment.getRentalId())
                .customerId(originalPayment.getCustomerId())
                .invoiceId(originalPayment.getInvoiceId())
                .paymentType(com.carrental.payment.model.PaymentType.REFUND)
                .amount(refundAmount)
                .paymentMethod(originalPayment.getPaymentMethod())
                .description("Refund for payment: " + originalPaymentId)
                .notes(reason)
                .status(PaymentStatus.COMPLETED)
                .build();
        
        refund.markAsCompleted("REFUND-" + System.currentTimeMillis());
        Payment saved = paymentRepository.save(refund);
        
        // Update original payment status
        originalPayment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(originalPayment);
        
        eventPublisher.publishRefundIssued(saved);
        
        log.info("Refund created successfully: {}", saved.getPaymentId());
        return saved;
    }
    
    @Transactional(readOnly = true)
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
    }
    
    @Transactional(readOnly = true)
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Payment> getPaymentsByCustomer(String customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByRental(String rentalId) {
        return paymentRepository.findByRentalId(rentalId);
    }
    
    @Transactional(readOnly = true)
    public Page<Payment> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = paymentRepository.getTotalRevenueByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    private boolean processWithPaymentGateway(Payment payment, String transactionRef) {
        // Mock payment gateway processing
        // In production, integrate with real payment gateway (Stripe, PayPal, etc.)
        log.info("Processing payment with gateway: {}, ref: {}", payment.getPaymentId(), transactionRef);
        
        // Simulate 95% success rate
        return Math.random() < 0.95;
    }
}
