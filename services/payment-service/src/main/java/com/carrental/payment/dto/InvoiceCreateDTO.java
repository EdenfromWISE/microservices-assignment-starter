package com.carrental.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceCreateDTO {
    
    @NotNull(message = "Rental ID is required")
    private String rentalId;
    
    @NotNull(message = "Customer ID is required")
    private String customerId;
    
    private BigDecimal rentalFee;
    
    private BigDecimal depositAmount;
    
    private BigDecimal penaltyAmount;
    
    private String notes;
}
