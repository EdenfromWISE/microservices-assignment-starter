package com.carrental.rental.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental_state_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalStateHistory {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "history_id", updatable = false, nullable = false)
    private String historyId;
    
    @Column(name = "rental_id", nullable = false)
    private String rentalId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private RentalStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private RentalStatus toStatus;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
