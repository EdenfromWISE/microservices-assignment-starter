package com.carrental.payment.dto;

import lombok.Data;

@Data
public class PaymentProcessDTO {
    private String transactionReference;
    private String notes;
}
