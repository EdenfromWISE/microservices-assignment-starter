package com.carrental.rental.repository;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, String> {
    
    Page<Rental> findByCustomerId(String customerId, Pageable pageable);
    
    Page<Rental> findByVehicleId(String vehicleId, Pageable pageable);
    
    Page<Rental> findByStatus(RentalStatus status, Pageable pageable);
    
    List<Rental> findByStatusAndStartDateBefore(RentalStatus status, LocalDateTime date);
    
    @Query("SELECT r FROM Rental r WHERE r.customerId = :customerId AND r.status = :status")
    List<Rental> findByCustomerIdAndStatus(String customerId, RentalStatus status);
    
    @Query("SELECT r FROM Rental r WHERE r.vehicleId = :vehicleId AND r.status IN :statuses")
    List<Rental> findActiveRentalsByVehicle(String vehicleId, List<RentalStatus> statuses);
    
    @Query("SELECT r FROM Rental r WHERE r.startDate BETWEEN :startDate AND :endDate")
    List<Rental> findRentalsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.status = :status")
    long countByStatus(RentalStatus status);
}
