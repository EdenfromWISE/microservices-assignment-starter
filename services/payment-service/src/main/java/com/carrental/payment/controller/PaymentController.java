package com.carrental.payment.controller;

import com.carrental.payment.dto.PaymentCreateDTO;
import com.carrental.payment.dto.PaymentProcessDTO;
import com.carrental.payment.model.Payment;
import com.carrental.payment.model.PaymentStatus;
import com.carrental.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentCreateDTO dto) {
        Payment payment = paymentService.createPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping
    public ResponseEntity<Page<Payment>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) PaymentStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Payment> payments;
        
        if (customerId != null) {
            payments = paymentService.getPaymentsByCustomer(customerId, pageable);
        } else if (status != null) {
            payments = paymentService.getPaymentsByStatus(status, pageable);
        } else {
            payments = paymentService.getAllPayments(pageable);
        }
        
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<Payment>> getPaymentsByRental(@PathVariable String rentalId) {
        List<Payment> payments = paymentService.getPaymentsByRental(rentalId);
        return ResponseEntity.ok(payments);
    }
    
    @PatchMapping("/{paymentId}/process")
    public ResponseEntity<Payment> processPayment(
            @PathVariable String paymentId,
            @RequestBody PaymentProcessDTO dto) {
        Payment payment = paymentService.processPayment(paymentId, dto);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> createRefund(
            @PathVariable String paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {
        Payment refund = paymentService.createRefund(paymentId, amount, reason != null ? reason : "Refund requested");
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }
}
