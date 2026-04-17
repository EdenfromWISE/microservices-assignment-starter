package com.carrental.damagepenalty.dto;

import com.carrental.damagepenalty.model.DamageType;
import com.carrental.damagepenalty.model.DamageSeverity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageReportDTO {
    
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;
    
    @NotNull(message = "Rental ID is required")
    private UUID rentalId;
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Damage type is required")
    private DamageType damageType;
    
    @NotNull(message = "Severity is required")
    private DamageSeverity severity;
    
    @NotNull(message = "Description is required")
    private String description;
    
    private String locationOnVehicle;
    
    private List<String> imageUrls;
    
    private String reportedBy;
}
