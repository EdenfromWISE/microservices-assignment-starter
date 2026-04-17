package com.carrental.payment.repository;

import com.carrental.payment.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    
    Optional<Invoice> findByRentalId(String rentalId);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    Page<Invoice> findByCustomerId(String customerId, Pageable pageable);
    
    Page<Invoice> findByIsPaid(Boolean isPaid, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.customerId = :customerId AND i.isPaid = :isPaid")
    Page<Invoice> findByCustomerIdAndIsPaid(String customerId, Boolean isPaid, Pageable pageable);
}
