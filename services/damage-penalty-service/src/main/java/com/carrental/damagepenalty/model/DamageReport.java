package com.carrental.damagepenalty.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "damage_reports", indexes = {
    @Index(name = "idx_damage_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_damage_rental", columnList = "rental_id"),
    @Index(name = "idx_damage_customer", columnList = "customer_id"),
    @Index(name = "idx_damage_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageReport {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "damage_id", updatable = false, nullable = false)
    private UUID damageId;
    
    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;
    
    @Column(name = "rental_id", nullable = false)
    private UUID rentalId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "damage_type", nullable = false, length = 50)
    private DamageType damageType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private DamageSeverity severity;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location_on_vehicle", length = 100)
    private String locationOnVehicle;
    
    @ElementCollection
    @CollectionTable(name = "damage_images", joinColumns = @JoinColumn(name = "damage_id"))
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls;
    
    @Column(name = "repair_cost", precision = 10, scale = 2)
    private BigDecimal repairCost;
    
    @Column(name = "reported_date")
    private LocalDateTime reportedDate;
    
    @Column(name = "reported_by", length = 50)
    private String reportedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DamageStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        reportedDate = LocalDateTime.now();
        status = DamageStatus.REPORTED;
        
        // Auto-calculate repair cost if not set
        if (repairCost == null && damageType != null && severity != null) {
            repairCost = calculateRepairCost();
        }
    }
    
    public BigDecimal calculateRepairCost() {
        if (damageType == null || severity == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal baseCost = getBaseCostForDamageType();
        BigDecimal multiplier = getMultiplierForSeverity();
        return baseCost.multiply(multiplier);
    }
    
    private BigDecimal getBaseCostForDamageType() {
        return switch (damageType) {
            case SCRATCH -> new BigDecimal("500000");
            case DENT -> new BigDecimal("1000000");
            case BROKEN_PART -> new BigDecimal("2000000");
            case INTERIOR_DAMAGE -> new BigDecimal("1500000");
            case MECHANICAL -> new BigDecimal("3000000");
        };
    }
    
    private BigDecimal getMultiplierForSeverity() {
        return switch (severity) {
            case MINOR -> new BigDecimal("1.0");
            case MODERATE -> new BigDecimal("2.0");
            case MAJOR -> new BigDecimal("4.0");
            case TOTAL_LOSS -> new BigDecimal("10.0");
        };
    }
}
