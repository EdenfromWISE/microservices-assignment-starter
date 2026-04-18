package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;
import com.carrental.rental.model.RentalStatus;

import java.time.LocalDateTime;

public class InProgressState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        throw new IllegalStateException("Cannot confirm rental in IN_PROGRESS state.");
    }
    
    @Override
    public void pickup(Rental rental) {
        // Already picked up
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        rental.setActualReturnDate(LocalDateTime.now());
        rental.setStatus(RentalStatus.INSPECTION);
        rental.setCurrentState(new InspectionState());
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        throw new IllegalStateException("Cannot complete inspection in IN_PROGRESS state. Must return vehicle first.");
    }
    
    @Override
    public void complete(Rental rental) {
        throw new IllegalStateException("Cannot complete rental in IN_PROGRESS state. Must return vehicle first.");
    }
    
    @Override
    public void cancel(Rental rental) {
        throw new IllegalStateException("Cannot cancel rental in IN_PROGRESS state. Vehicle must be returned first.");
    }
}
