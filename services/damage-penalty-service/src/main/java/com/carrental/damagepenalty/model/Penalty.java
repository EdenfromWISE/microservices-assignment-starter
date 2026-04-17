package com.carrental.damagepenalty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "penalties", indexes = {
    @Index(name = "idx_penalty_customer", columnList = "customer_id"),
    @Index(name = "idx_penalty_rental", columnList = "rental_id"),
    @Index(name = "idx_penalty_status", columnList = "payment_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Penalty {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "penalty_id")
    private UUID penaltyId;
    
    @Column(name = "damage_id")
    private UUID damageId;
    
    @Column(name = "rental_id", nullable = false)
    private UUID rentalId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type", nullable = false, length = 50)
    private PenaltyType penaltyType;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "penalty_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal penaltyAmount;
    
    @Column(name = "penalty_date", nullable = false)
    private LocalDate penaltyDate;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;
    
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "payment_date")
    private LocalDate paymentDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        penaltyDate = LocalDate.now();
        paymentStatus = PaymentStatus.UNPAID;
        paidAmount = BigDecimal.ZERO;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void addPayment(BigDecimal amount) {
        this.paidAmount = this.paidAmount.add(amount);
        
        if (this.paidAmount.compareTo(this.penaltyAmount) >= 0) {
            this.paymentStatus = PaymentStatus.PAID;
            this.paymentDate = LocalDate.now();
        } else {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
}
