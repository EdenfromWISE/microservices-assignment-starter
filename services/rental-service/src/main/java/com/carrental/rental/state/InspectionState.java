package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;
import com.carrental.rental.model.RentalStatus;

public class InspectionState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        throw new IllegalStateException("Cannot confirm rental in INSPECTION state.");
    }
    
    @Override
    public void pickup(Rental rental) {
        throw new IllegalStateException("Cannot pickup vehicle in INSPECTION state.");
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        // Already returned
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        rental.setHasDamage(hasDamage);
        
        if (hasDamage) {
            rental.setStatus(RentalStatus.PENALTY_DUE);
            rental.setCurrentState(new PenaltyDueState());
        } else {
            rental.setStatus(RentalStatus.COMPLETED);
            rental.setCurrentState(new CompletedState());
        }
    }
    
    @Override
    public void complete(Rental rental) {
        throw new IllegalStateException("Cannot complete rental in INSPECTION state. Must complete inspection first.");
    }
    
    @Override
    public void cancel(Rental rental) {
        throw new IllegalStateException("Cannot cancel rental in INSPECTION state.");
    }
}
