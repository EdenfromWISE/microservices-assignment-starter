package com.carrental.damagepenalty.service;

import com.carrental.damagepenalty.dto.PenaltyDTO;
import com.carrental.damagepenalty.dto.PenaltyPaymentDTO;
import com.carrental.damagepenalty.event.PenaltyEventPublisher;
import com.carrental.damagepenalty.exception.PenaltyNotFoundException;
import com.carrental.damagepenalty.model.*;
import com.carrental.damagepenalty.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PenaltyService {
    
    private final PenaltyRepository penaltyRepository;
    private final PenaltyEventPublisher eventPublisher;
    
    @Transactional
    public Penalty createPenalty(PenaltyDTO dto) {
        log.info("Creating penalty for rental: {}, type: {}", 
                 dto.getRentalId(), dto.getPenaltyType());
        
        Penalty penalty = Penalty.builder()
            .damageId(dto.getDamageId())
            .rentalId(dto.getRentalId())
            .customerId(dto.getCustomerId())
            .vehicleId(dto.getVehicleId())
            .penaltyType(dto.getPenaltyType())
            .description(dto.getDescription())
            .penaltyAmount(dto.getPenaltyAmount())
            .dueDate(dto.getDueDate())
            .build();
        
        penalty = penaltyRepository.save(penalty);
        
        // Publish event
        eventPublisher.publishPenaltyCreated(penalty);
        
        log.info("Penalty created with ID: {}", penalty.getPenaltyId());
        return penalty;
    }
    
    @Transactional
    public Penalty createPenaltyForDamage(DamageReport damage) {
        PenaltyDTO dto = PenaltyDTO.builder()
            .damageId(damage.getDamageId())
            .rentalId(damage.getRentalId())
            .customerId(damage.getCustomerId())
            .vehicleId(damage.getVehicleId())
            .penaltyType(PenaltyType.DAMAGE)
            .description("Repair cost for " + damage.getDamageType())
            .penaltyAmount(damage.getRepairCost())
            .dueDate(LocalDate.now().plusDays(30))
            .build();
        
        return createPenalty(dto);
    }
    
    @Transactional
    public Penalty payPenalty(UUID penaltyId, PenaltyPaymentDTO paymentDTO) {
        log.info("Processing payment for penalty: {}, amount: {}", 
                 penaltyId, paymentDTO.getAmount());
        
        Penalty penalty = penaltyRepository.findById(penaltyId)
            .orElseThrow(() -> new PenaltyNotFoundException("Penalty not found with ID: " + penaltyId));
        
        penalty.addPayment(paymentDTO.getAmount());
        penalty = penaltyRepository.save(penalty);
        
        // Publish event
        eventPublisher.publishPenaltyPaid(penalty, paymentDTO.getAmount());
        
        log.info("Penalty {} payment processed. Status: {}", 
                 penaltyId, penalty.getPaymentStatus());
        return penalty;
    }
    
    @Transactional(readOnly = true)
    public Page<Penalty> getPenalties(UUID customerId, UUID rentalId, 
                                       PaymentStatus status, Pageable pageable) {
        if (customerId != null && status != null) {
            return penaltyRepository.findByCustomerIdAndPaymentStatus(
                customerId, status, pageable);
        } else if (customerId != null) {
            return penaltyRepository.findByCustomerId(customerId, pageable);
        } else if (rentalId != null) {
            return penaltyRepository.findByRentalId(rentalId, pageable);
        } else if (status != null) {
            return penaltyRepository.findByPaymentStatus(status, pageable);
        }
        return penaltyRepository.findAll(pageable);
    }
}
