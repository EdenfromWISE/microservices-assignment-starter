package com.carrental.payment.service;

import com.carrental.payment.dto.InvoiceCreateDTO;
import com.carrental.payment.event.PaymentEventPublisher;
import com.carrental.payment.exception.InvoiceNotFoundException;
import com.carrental.payment.model.Invoice;
import com.carrental.payment.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final PaymentEventPublisher eventPublisher;
    
    @Transactional
    public Invoice createInvoice(InvoiceCreateDTO dto) {
        log.info("Creating invoice for rental: {}", dto.getRentalId());
        
        Invoice invoice = Invoice.builder()
                .rentalId(dto.getRentalId())
                .customerId(dto.getCustomerId())
                .issueDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .rentalFee(dto.getRentalFee())
                .depositAmount(dto.getDepositAmount() != null ? dto.getDepositAmount() : BigDecimal.ZERO)
                .penaltyAmount(dto.getPenaltyAmount() != null ? dto.getPenaltyAmount() : BigDecimal.ZERO)
                .notes(dto.getNotes())
                .build();
        
        invoice.calculateTotal();
        Invoice saved = invoiceRepository.save(invoice);
        
        eventPublisher.publishInvoiceCreated(saved);
        
        log.info("Invoice created: {} - {}", saved.getInvoiceId(), saved.getInvoiceNumber());
        return saved;
    }
    
    @Transactional
    public Invoice updateInvoicePenalty(String invoiceId, BigDecimal penaltyAmount) {
        log.info("Updating invoice penalty: {}, amount: {}", invoiceId, penaltyAmount);
        Invoice invoice = getInvoiceById(invoiceId);
        
        invoice.setPenaltyAmount(penaltyAmount);
        invoice.calculateTotal();
        
        return invoiceRepository.save(invoice);
    }
    
    @Transactional
    public Invoice markInvoiceAsPaid(String invoiceId) {
        log.info("Marking invoice as paid: {}", invoiceId);
        Invoice invoice = getInvoiceById(invoiceId);
        
        invoice.markAsPaid();
        Invoice saved = invoiceRepository.save(invoice);
        
        eventPublisher.publishInvoicePaid(saved);
        
        log.info("Invoice marked as paid: {}", invoiceId);
        return saved;
    }
    
    @Transactional
    public Invoice addRefund(String invoiceId, BigDecimal refundAmount) {
        log.info("Adding refund to invoice: {}, amount: {}", invoiceId, refundAmount);
        Invoice invoice = getInvoiceById(invoiceId);
        
        BigDecimal currentRefund = invoice.getRefundAmount() != null ? invoice.getRefundAmount() : BigDecimal.ZERO;
        invoice.setRefundAmount(currentRefund.add(refundAmount));
        invoice.calculateTotal();
        
        return invoiceRepository.save(invoice);
    }
    
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + invoiceId));
    }
    
    @Transactional(readOnly = true)
    public Invoice getInvoiceByRentalId(String rentalId) {
        return invoiceRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found for rental: " + rentalId));
    }
    
    @Transactional(readOnly = true)
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + invoiceNumber));
    }
    
    @Transactional(readOnly = true)
    public Page<Invoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByCustomer(String customerId, Pageable pageable) {
        return invoiceRepository.findByCustomerId(customerId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Invoice> getUnpaidInvoices(Pageable pageable) {
        return invoiceRepository.findByIsPaid(false, pageable);
    }
}
