package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;
import com.carrental.rental.model.RentalStatus;

public class PenaltyDueState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        throw new IllegalStateException("Cannot confirm rental in PENALTY_DUE state.");
    }
    
    @Override
    public void pickup(Rental rental) {
        throw new IllegalStateException("Cannot pickup vehicle in PENALTY_DUE state.");
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        throw new IllegalStateException("Vehicle already returned in PENALTY_DUE state.");
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        throw new IllegalStateException("Inspection already completed in PENALTY_DUE state.");
    }
    
    @Override
    public void complete(Rental rental) {
        // Complete after penalty is paid
        rental.setStatus(RentalStatus.COMPLETED);
        rental.setCurrentState(new CompletedState());
    }
    
    @Override
    public void cancel(Rental rental) {
        throw new IllegalStateException("Cannot cancel rental in PENALTY_DUE state.");
    }
}
