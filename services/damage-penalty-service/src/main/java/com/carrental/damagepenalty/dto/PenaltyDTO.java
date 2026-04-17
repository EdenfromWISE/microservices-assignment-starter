package com.carrental.damagepenalty.dto;

import com.carrental.damagepenalty.model.PenaltyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyDTO {
    
    private UUID damageId;
    
    @NotNull(message = "Rental ID is required")
    private UUID rentalId;
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;
    
    @NotNull(message = "Penalty type is required")
    private PenaltyType penaltyType;
    
    @NotNull(message = "Description is required")
    private String description;
    
    @NotNull(message = "Penalty amount is required")
    @Positive(message = "Penalty amount must be positive")
    private BigDecimal penaltyAmount;
    
    private LocalDate dueDate;
}
