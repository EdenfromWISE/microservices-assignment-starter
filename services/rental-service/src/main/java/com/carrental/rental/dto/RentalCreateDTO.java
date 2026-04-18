package com.carrental.rental.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RentalCreateDTO {
    
    @NotNull(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Vehicle ID is required")
    private String vehicleId;
    
    private String partnerId;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    @NotNull(message = "Pickup location is required")
    private String pickupLocation;
    
    @NotNull(message = "Return location is required")
    private String returnLocation;
    
    @NotNull(message = "Daily rate is required")
    @Positive(message = "Daily rate must be positive")
    private BigDecimal dailyRate;
    
    private BigDecimal depositAmount;
}
