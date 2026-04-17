package com.carrental.damagepenalty.service;

import com.carrental.damagepenalty.dto.DamageReportDTO;
import com.carrental.damagepenalty.event.DamageEventPublisher;
import com.carrental.damagepenalty.exception.DamageNotFoundException;
import com.carrental.damagepenalty.model.*;
import com.carrental.damagepenalty.repository.DamageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DamageService {
    
    private final DamageRepository damageRepository;
    private final PenaltyService penaltyService;
    private final DamageEventPublisher eventPublisher;
    
    @Transactional
    public DamageReport createDamageReport(DamageReportDTO dto) {
        log.info("Creating damage report for vehicle: {}, rental: {}", 
                 dto.getVehicleId(), dto.getRentalId());
        
        DamageReport damage = DamageReport.builder()
            .vehicleId(dto.getVehicleId())
            .rentalId(dto.getRentalId())
            .customerId(dto.getCustomerId())
            .damageType(dto.getDamageType())
            .severity(dto.getSeverity())
            .description(dto.getDescription())
            .locationOnVehicle(dto.getLocationOnVehicle())
            .imageUrls(dto.getImageUrls())
            .reportedBy(dto.getReportedBy())
            .build();
        
        // Calculate repair cost
        damage.setRepairCost(damage.calculateRepairCost());
        
        // Save to database
        damage = damageRepository.save(damage);
        
        // Publish event
        eventPublisher.publishDamageReported(damage);
        
        // Auto-create penalty for moderate and major damages
        if (damage.getSeverity() == DamageSeverity.MODERATE || 
            damage.getSeverity() == DamageSeverity.MAJOR) {
            penaltyService.createPenaltyForDamage(damage);
        }
        
        log.info("Damage report created with ID: {}", damage.getDamageId());
        return damage;
    }
    
    @Transactional(readOnly = true)
    public DamageReport getDamageReport(UUID damageId) {
        return damageRepository.findById(damageId)
            .orElseThrow(() -> new DamageNotFoundException("Damage report not found with ID: " + damageId));
    }
    
    @Transactional(readOnly = true)
    public Page<DamageReport> getDamageReports(UUID vehicleId, UUID rentalId, 
                                                DamageStatus status, Pageable pageable) {
        if (vehicleId != null && status != null) {
            return damageRepository.findByVehicleIdAndStatus(vehicleId, status, pageable);
        } else if (vehicleId != null) {
            return damageRepository.findByVehicleId(vehicleId, pageable);
        } else if (rentalId != null) {
            return damageRepository.findByRentalId(rentalId, pageable);
        } else if (status != null) {
            return damageRepository.findByStatus(status, pageable);
        }
        return damageRepository.findAll(pageable);
    }
    
    @Transactional
    public DamageReport updateDamageStatus(UUID damageId, DamageStatus status, 
                                           BigDecimal repairCost) {
        DamageReport damage = getDamageReport(damageId);
        
        damage.setStatus(status);
        if (repairCost != null) {
            damage.setRepairCost(repairCost);
        }
        
        damage = damageRepository.save(damage);
        
        // Publish event
        eventPublisher.publishDamageUpdated(damage);
        
        log.info("Damage report {} updated to status: {}", damageId, status);
        return damage;
    }
}
