package com.carrental.rental.controller;

import com.carrental.rental.dto.InspectionDTO;
import com.carrental.rental.dto.RentalCreateDTO;
import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalStateHistory;
import com.carrental.rental.model.RentalStatus;
import com.carrental.rental.service.RentalService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    
    private final RentalService rentalService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
    
    @PostMapping
    public ResponseEntity<Rental> createRental(@Valid @RequestBody RentalCreateDTO dto) {
        Rental rental = rentalService.createRental(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }
    
    @GetMapping("/{rentalId}")
    public ResponseEntity<Rental> getRentalById(@PathVariable String rentalId) {
        Rental rental = rentalService.getRentalById(rentalId);
        return ResponseEntity.ok(rental);
    }
    
    @GetMapping
    public ResponseEntity<Page<Rental>> getAllRentals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(required = false) RentalStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Rental> rentals;
        
        if (customerId != null) {
            rentals = rentalService.getRentalsByCustomer(customerId, pageable);
        } else if (vehicleId != null) {
            rentals = rentalService.getRentalsByVehicle(vehicleId, pageable);
        } else if (status != null) {
            rentals = rentalService.getRentalsByStatus(status, pageable);
        } else {
            rentals = rentalService.getAllRentals(pageable);
        }
        
        return ResponseEntity.ok(rentals);
    }
    
    @PatchMapping("/{rentalId}/confirm")
    public ResponseEntity<Rental> confirmRental(@PathVariable String rentalId) {
        Rental rental = rentalService.confirmRental(rentalId);
        return ResponseEntity.ok(rental);
    }
    
    @PatchMapping("/{rentalId}/pickup")
    public ResponseEntity<Rental> pickupVehicle(@PathVariable String rentalId) {
        Rental rental = rentalService.pickupVehicle(rentalId);
        return ResponseEntity.ok(rental);
    }
    
    @PatchMapping("/{rentalId}/return")
    public ResponseEntity<Rental> returnVehicle(@PathVariable String rentalId) {
        Rental rental = rentalService.returnVehicle(rentalId);
        return ResponseEntity.ok(rental);
    }
    
    @PatchMapping("/{rentalId}/inspection")
    public ResponseEntity<Rental> completeInspection(
            @PathVariable String rentalId,
            @RequestBody InspectionDTO dto) {
        Rental rental = rentalService.completeInspection(rentalId, dto);
        return ResponseEntity.ok(rental);
    }
    
    @PatchMapping("/{rentalId}/complete")
    public ResponseEntity<Rental> completeRental(@PathVariable String rentalId) {
        Rental rental = rentalService.completeRental(rentalId);
        return ResponseEntity.ok(rental);
    }
    
    @PatchMapping("/{rentalId}/cancel")
    public ResponseEntity<Rental> cancelRental(
            @PathVariable String rentalId,
            @RequestParam(required = false) String reason) {
        Rental rental = rentalService.cancelRental(rentalId, reason != null ? reason : "No reason provided");
        return ResponseEntity.ok(rental);
    }
    
    @PatchMapping("/{rentalId}/penalty")
    public ResponseEntity<Rental> updatePenalty(
            @PathVariable String rentalId,
            @RequestParam BigDecimal amount) {
        Rental rental = rentalService.updatePenaltyAmount(rentalId, amount);
        return ResponseEntity.ok(rental);
    }
    
    @GetMapping("/{rentalId}/history")
    public ResponseEntity<List<RentalStateHistory>> getRentalHistory(@PathVariable String rentalId) {
        List<RentalStateHistory> history = rentalService.getRentalHistory(rentalId);
        return ResponseEntity.ok(history);
    }
}
