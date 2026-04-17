package com.carrental.payment.repository;

import com.carrental.payment.model.Payment;
import com.carrental.payment.model.PaymentStatus;
import com.carrental.payment.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    Page<Payment> findByCustomerId(String customerId, Pageable pageable);
    
    List<Payment> findByRentalId(String rentalId);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    Page<Payment> findByPaymentType(PaymentType type, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
    List<Payment> findByCustomerIdAndStatus(String customerId, PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
