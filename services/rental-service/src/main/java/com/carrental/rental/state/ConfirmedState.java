package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;
import com.carrental.rental.model.RentalStatus;

public class ConfirmedState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        // Already confirmed, do nothing or log
    }
    
    @Override
    public void pickup(Rental rental) {
        rental.setStatus(RentalStatus.IN_PROGRESS);
        rental.setCurrentState(new InProgressState());
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        throw new IllegalStateException("Cannot return vehicle in CONFIRMED state. Must pickup first.");
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        throw new IllegalStateException("Cannot complete inspection in CONFIRMED state.");
    }
    
    @Override
    public void complete(Rental rental) {
        throw new IllegalStateException("Cannot complete rental in CONFIRMED state.");
    }
    
    @Override
    public void cancel(Rental rental) {
        rental.setStatus(RentalStatus.CANCELLED);
        rental.setCurrentState(new CancelledState());
    }
}
