package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;
import com.carrental.rental.model.RentalStatus;

public class PendingState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        rental.setStatus(RentalStatus.CONFIRMED);
        rental.setCurrentState(new ConfirmedState());
    }
    
    @Override
    public void pickup(Rental rental) {
        throw new IllegalStateException("Cannot pickup vehicle in PENDING state. Must confirm first.");
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        throw new IllegalStateException("Cannot return vehicle in PENDING state.");
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        throw new IllegalStateException("Cannot complete inspection in PENDING state.");
    }
    
    @Override
    public void complete(Rental rental) {
        throw new IllegalStateException("Cannot complete rental in PENDING state.");
    }
    
    @Override
    public void cancel(Rental rental) {
        rental.setStatus(RentalStatus.CANCELLED);
        rental.setCurrentState(new CancelledState());
    }
}
