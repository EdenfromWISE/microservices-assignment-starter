package com.carrental.rental.service;

import com.carrental.rental.dto.InspectionDTO;
import com.carrental.rental.dto.RentalCreateDTO;
import com.carrental.rental.event.RentalEventPublisher;
import com.carrental.rental.exception.RentalNotFoundException;
import com.carrental.rental.model.*;
import com.carrental.rental.repository.RentalRepository;
import com.carrental.rental.repository.RentalStateHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalService {
    
    private final RentalRepository rentalRepository;
    private final RentalStateHistoryRepository stateHistoryRepository;
    private final RentalEventPublisher eventPublisher;
    
    @Transactional
    public Rental createRental(RentalCreateDTO dto) {
        log.info("Creating rental for customer {} and vehicle {}", dto.getCustomerId(), dto.getVehicleId());
        
        Rental rental = Rental.builder()
                .customerId(dto.getCustomerId())
                .vehicleId(dto.getVehicleId())
                .partnerId(dto.getPartnerId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .pickupLocation(dto.getPickupLocation())
                .returnLocation(dto.getReturnLocation())
                .dailyRate(dto.getDailyRate())
                .depositAmount(dto.getDepositAmount() != null ? dto.getDepositAmount() : dto.getDailyRate().multiply(BigDecimal.valueOf(2)))
                .status(RentalStatus.PENDING)
                .build();
        
        rental.calculateTotalCost();
        Rental saved = rentalRepository.save(rental);
        
        saveStateHistory(saved.getRentalId(), null, RentalStatus.PENDING, "Rental created");
        eventPublisher.publishRentalCreated(saved);
        
        log.info("Rental created successfully: {}", saved.getRentalId());
        return saved;
    }
    
    @Transactional
    public Rental confirmRental(String rentalId) {
        log.info("Confirming rental: {}", rentalId);
        Rental rental = getRentalById(rentalId);
        
        RentalStatus oldStatus = rental.getStatus();
        rental.confirm();
        Rental saved = rentalRepository.save(rental);
        
        saveStateHistory(rentalId, oldStatus, rental.getStatus(), "Rental confirmed");
        eventPublisher.publishRentalConfirmed(saved);
        
        return saved;
    }
    
    @Transactional
    public Rental pickupVehicle(String rentalId) {
        log.info("Processing vehicle pickup for rental: {}", rentalId);
        Rental rental = getRentalById(rentalId);
        
        RentalStatus oldStatus = rental.getStatus();
        rental.pickup();
        Rental saved = rentalRepository.save(rental);
        
        saveStateHistory(rentalId, oldStatus, rental.getStatus(), "Vehicle picked up");
        eventPublisher.publishVehiclePickedUp(saved);
        
        return saved;
    }
    
    @Transactional
    public Rental returnVehicle(String rentalId) {
        log.info("Processing vehicle return for rental: {}", rentalId);
        Rental rental = getRentalById(rentalId);
        
        RentalStatus oldStatus = rental.getStatus();
        rental.returnVehicle();
        Rental saved = rentalRepository.save(rental);
        
        saveStateHistory(rentalId, oldStatus, rental.getStatus(), "Vehicle returned, awaiting inspection");
        eventPublisher.publishVehicleReturned(saved);
        
        return saved;
    }
    
    @Transactional
    public Rental completeInspection(String rentalId, InspectionDTO dto) {
        log.info("Completing inspection for rental: {}", rentalId);
        Rental rental = getRentalById(rentalId);
        
        rental.setInspectionNotes(dto.getInspectionNotes());
        rental.setDamageReportId(dto.getDamageReportId());
        
        RentalStatus oldStatus = rental.getStatus();
        rental.completeInspection(dto.isHasDamage());
        Rental saved = rentalRepository.save(rental);
        
        String notes = dto.isHasDamage() ? "Inspection completed - damage found" : "Inspection completed - no damage";
        saveStateHistory(rentalId, oldStatus, rental.getStatus(), notes);
        eventPublisher.publishInspectionCompleted(saved, dto.isHasDamage());
        
        return saved;
    }
    
    @Transactional
    public Rental completeRental(String rentalId) {
        log.info("Completing rental: {}", rentalId);
        Rental rental = getRentalById(rentalId);
        
        RentalStatus oldStatus = rental.getStatus();
        rental.complete();
        Rental saved = rentalRepository.save(rental);
        
        saveStateHistory(rentalId, oldStatus, rental.getStatus(), "Rental completed");
        eventPublisher.publishRentalCompleted(saved);
        
        return saved;
    }
    
    @Transactional
    public Rental cancelRental(String rentalId, String reason) {
        log.info("Cancelling rental: {} - Reason: {}", rentalId, reason);
        Rental rental = getRentalById(rentalId);
        
        RentalStatus oldStatus = rental.getStatus();
        rental.cancel();
        Rental saved = rentalRepository.save(rental);
        
        saveStateHistory(rentalId, oldStatus, rental.getStatus(), "Rental cancelled: " + reason);
        eventPublisher.publishRentalCancelled(saved);
        
        return saved;
    }
    
    @Transactional
    public Rental updatePenaltyAmount(String rentalId, BigDecimal penaltyAmount) {
        log.info("Updating penalty amount for rental {}: {}", rentalId, penaltyAmount);
        Rental rental = getRentalById(rentalId);
        RentalStatus oldStatus = rental.getStatus();
        rental.setPenaltyAmount(penaltyAmount);

        if (penaltyAmount != null
                && penaltyAmount.compareTo(BigDecimal.ZERO) > 0
                && rental.getStatus() == RentalStatus.INSPECTION) {
            rental.setStatus(RentalStatus.PENALTY_DUE);
        }

        Rental saved = rentalRepository.save(rental);
        if (oldStatus != saved.getStatus()) {
            saveStateHistory(
                    rentalId,
                    oldStatus,
                    saved.getStatus(),
                    "Auto transition after penalty update");
        }

        return saved;
    }
    
    @Transactional(readOnly = true)
    public Rental getRentalById(String rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException("Rental not found: " + rentalId));
    }
    
    @Transactional(readOnly = true)
    public Page<Rental> getAllRentals(Pageable pageable) {
        return rentalRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Rental> getRentalsByCustomer(String customerId, Pageable pageable) {
        return rentalRepository.findByCustomerId(customerId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Rental> getRentalsByVehicle(String vehicleId, Pageable pageable) {
        return rentalRepository.findByVehicleId(vehicleId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Rental> getRentalsByStatus(RentalStatus status, Pageable pageable) {
        return rentalRepository.findByStatus(status, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<RentalStateHistory> getRentalHistory(String rentalId) {
        return stateHistoryRepository.findByRentalIdOrderByChangedAtDesc(rentalId);
    }
    
    private void saveStateHistory(String rentalId, RentalStatus fromStatus, RentalStatus toStatus, String notes) {
        RentalStateHistory history = RentalStateHistory.builder()
                .rentalId(rentalId)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .notes(notes)
                .build();
        stateHistoryRepository.save(history);
    }
}
