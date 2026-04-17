package com.carrental.damagepenalty.controller;

import com.carrental.damagepenalty.dto.DamageReportDTO;
import com.carrental.damagepenalty.model.DamageReport;
import com.carrental.damagepenalty.model.DamageStatus;
import com.carrental.damagepenalty.service.DamageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/damage-reports")
@RequiredArgsConstructor
public class DamageController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private final DamageService damageService;
    
    @PostMapping
    public ResponseEntity<DamageReport> createDamageReport(
            @Valid @RequestBody DamageReportDTO dto) {
        DamageReport damage = damageService.createDamageReport(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(damage);
    }
    
    @GetMapping("/{damageId}")
    public ResponseEntity<DamageReport> getDamageReport(@PathVariable UUID damageId) {
        DamageReport damage = damageService.getDamageReport(damageId);
        return ResponseEntity.ok(damage);
    }
    
    @GetMapping
    public ResponseEntity<Page<DamageReport>> getDamageReports(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID rentalId,
            @RequestParam(required = false) DamageStatus status,
            Pageable pageable) {
        Page<DamageReport> damages = damageService.getDamageReports(
            vehicleId, rentalId, status, pageable);
        return ResponseEntity.ok(damages);
    }
    
    @PatchMapping("/{damageId}")
    public ResponseEntity<DamageReport> updateDamageStatus(
            @PathVariable UUID damageId,
            @RequestParam DamageStatus status,
            @RequestParam(required = false) BigDecimal repairCost) {
        DamageReport damage = damageService.updateDamageStatus(
            damageId, status, repairCost);
        return ResponseEntity.ok(damage);
    }
}
