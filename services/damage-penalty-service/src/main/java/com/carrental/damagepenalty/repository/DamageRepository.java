package com.carrental.damagepenalty.repository;

import com.carrental.damagepenalty.model.DamageReport;
import com.carrental.damagepenalty.model.DamageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DamageRepository extends JpaRepository<DamageReport, UUID> {
    
    Page<DamageReport> findByVehicleId(UUID vehicleId, Pageable pageable);
    
    Page<DamageReport> findByRentalId(UUID rentalId, Pageable pageable);
    
    Page<DamageReport> findByCustomerId(UUID customerId, Pageable pageable);
    
    Page<DamageReport> findByStatus(DamageStatus status, Pageable pageable);
    
    Page<DamageReport> findByVehicleIdAndStatus(UUID vehicleId, DamageStatus status, Pageable pageable);
}
