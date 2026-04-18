package com.carrental.rental.repository;

import com.carrental.rental.model.RentalStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalStateHistoryRepository extends JpaRepository<RentalStateHistory, String> {
    List<RentalStateHistory> findByRentalIdOrderByChangedAtDesc(String rentalId);
}
