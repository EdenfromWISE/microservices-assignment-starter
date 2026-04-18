package com.carrental.rental.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "rental_id", updatable = false, nullable = false)
    private String rentalId;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;
    
    @Column(name = "partner_id")
    private String partnerId;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;
    
    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;
    
    @Column(name = "return_location", nullable = false)
    private String returnLocation;
    
    @Column(name = "daily_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyRate;
    
    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;
    
    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RentalStatus status;
    
    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;
    
    @Builder.Default
    @Column(name = "has_damage")
    private Boolean hasDamage = false;
    
    @Column(name = "damage_report_id")
    private String damageReportId;
    
    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Transient
    @JsonIgnore
    private RentalState currentState;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RentalStatus.PENDING;
        }
        if (hasDamage == null) {
            hasDamage = false;
        }
        if (penaltyAmount == null) {
            penaltyAmount = BigDecimal.ZERO;
        }
        calculateTotalCost();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (hasDamage == null) {
            hasDamage = false;
        }
        if (penaltyAmount == null) {
            penaltyAmount = BigDecimal.ZERO;
        }
    }
    
    @PostLoad
    protected void initializeState() {
        this.currentState = RentalStateFactory.getState(this.status);
    }
    
    public void calculateTotalCost() {
        if (startDate != null && endDate != null && dailyRate != null) {
            long days = java.time.Duration.between(startDate, endDate).toDays();
            if (days < 1) days = 1;
            this.totalCost = dailyRate.multiply(BigDecimal.valueOf(days));
        }
    }
    
    // State Pattern methods
    public void confirm() {
        if (currentState != null) {
            currentState.confirm(this);
        }
    }
    
    public void pickup() {
        if (currentState != null) {
            currentState.pickup(this);
        }
    }
    
    public void returnVehicle() {
        if (currentState != null) {
            currentState.returnVehicle(this);
        }
    }
    
    public void completeInspection(boolean hasDamage) {
        if (currentState != null) {
            currentState.completeInspection(this, hasDamage);
        }
    }
    
    public void complete() {
        if (currentState != null) {
            currentState.complete(this);
        }
    }
    
    public void cancel() {
        if (currentState != null) {
            currentState.cancel(this);
        }
    }
}
