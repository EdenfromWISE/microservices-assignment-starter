package com.carrental.damagepenalty.controller;

import com.carrental.damagepenalty.dto.PenaltyDTO;
import com.carrental.damagepenalty.dto.PenaltyPaymentDTO;
import com.carrental.damagepenalty.model.Penalty;
import com.carrental.damagepenalty.model.PaymentStatus;
import com.carrental.damagepenalty.service.PenaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {
    
    private final PenaltyService penaltyService;
    
    @PostMapping
    public ResponseEntity<Penalty> createPenalty(@Valid @RequestBody PenaltyDTO dto) {
        Penalty penalty = penaltyService.createPenalty(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(penalty);
    }
    
    @GetMapping
    public ResponseEntity<Page<Penalty>> getPenalties(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID rentalId,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        Page<Penalty> penalties = penaltyService.getPenalties(
            customerId, rentalId, status, pageable);
        return ResponseEntity.ok(penalties);
    }
    
    @PostMapping("/{penaltyId}/pay")
    public ResponseEntity<Penalty> payPenalty(
            @PathVariable UUID penaltyId,
            @Valid @RequestBody PenaltyPaymentDTO paymentDTO) {
        Penalty penalty = penaltyService.payPenalty(penaltyId, paymentDTO);
        return ResponseEntity.ok(penalty);
    }
}
