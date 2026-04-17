package com.carrental.damagepenalty.repository;

import com.carrental.damagepenalty.model.Penalty;
import com.carrental.damagepenalty.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, UUID> {
    
    Page<Penalty> findByCustomerId(UUID customerId, Pageable pageable);
    
    Page<Penalty> findByRentalId(UUID rentalId, Pageable pageable);
    
    Page<Penalty> findByPaymentStatus(PaymentStatus status, Pageable pageable);
    
    Page<Penalty> findByCustomerIdAndPaymentStatus(UUID customerId, PaymentStatus status, Pageable pageable);
}
