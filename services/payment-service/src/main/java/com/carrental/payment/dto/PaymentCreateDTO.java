package com.carrental.payment.dto;

import com.carrental.payment.model.PaymentMethod;
import com.carrental.payment.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateDTO {
    
    private String rentalId;
    
    @NotNull(message = "Customer ID is required")
    private String customerId;
    
    private String invoiceId;
    
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String description;
    
    private String notes;
}
