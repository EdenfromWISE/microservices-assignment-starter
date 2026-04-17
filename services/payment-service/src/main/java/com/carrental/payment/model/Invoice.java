package com.carrental.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "invoice_id", updatable = false, nullable = false)
    private String invoiceId;
    
    @Column(name = "rental_id", nullable = false)
    private String rentalId;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "rental_fee", precision = 15, scale = 2)
    private BigDecimal rentalFee;
    
    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount;
    
    @Column(name = "penalty_amount", precision = 15, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    @Column(name = "is_paid")
    private Boolean isPaid = false;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        generateInvoiceNumber();
        calculateTotal();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private void generateInvoiceNumber() {
        if (invoiceNumber == null) {
            long timestamp = System.currentTimeMillis();
            this.invoiceNumber = "INV-" + timestamp;
        }
    }
    
    public void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (rentalFee != null) total = total.add(rentalFee);
        if (penaltyAmount != null) total = total.add(penaltyAmount);
        if (depositAmount != null) total = total.subtract(depositAmount);
        this.totalAmount = total;
    }
    
    public void markAsPaid() {
        this.isPaid = true;
        this.paidAt = LocalDateTime.now();
        this.paidAmount = this.totalAmount;
    }
    
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }
}
