package com.carrental.payment.controller;

import com.carrental.payment.dto.InvoiceCreateDTO;
import com.carrental.payment.model.Invoice;
import com.carrental.payment.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody InvoiceCreateDTO dto) {
        Invoice invoice = invoiceService.createInvoice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }
    
    @GetMapping("/{invoiceId}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable String invoiceId) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<Invoice> getInvoiceByRental(@PathVariable String rentalId) {
        Invoice invoice = invoiceService.getInvoiceByRentalId(rentalId);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping
    public ResponseEntity<Page<Invoice>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Boolean unpaidOnly) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Invoice> invoices;
        
        if (unpaidOnly != null && unpaidOnly) {
            invoices = invoiceService.getUnpaidInvoices(pageable);
        } else if (customerId != null) {
            invoices = invoiceService.getInvoicesByCustomer(customerId, pageable);
        } else {
            invoices = invoiceService.getAllInvoices(pageable);
        }
        
        return ResponseEntity.ok(invoices);
    }
    
    @PatchMapping("/{invoiceId}/penalty")
    public ResponseEntity<Invoice> updatePenalty(
            @PathVariable String invoiceId,
            @RequestParam BigDecimal amount) {
        Invoice invoice = invoiceService.updateInvoicePenalty(invoiceId, amount);
        return ResponseEntity.ok(invoice);
    }
    
    @PatchMapping("/{invoiceId}/paid")
    public ResponseEntity<Invoice> markAsPaid(@PathVariable String invoiceId) {
        Invoice invoice = invoiceService.markInvoiceAsPaid(invoiceId);
        return ResponseEntity.ok(invoice);
    }
    
    @PatchMapping("/{invoiceId}/refund")
    public ResponseEntity<Invoice> addRefund(
            @PathVariable String invoiceId,
            @RequestParam BigDecimal amount) {
        Invoice invoice = invoiceService.addRefund(invoiceId, amount);
        return ResponseEntity.ok(invoice);
    }
}
