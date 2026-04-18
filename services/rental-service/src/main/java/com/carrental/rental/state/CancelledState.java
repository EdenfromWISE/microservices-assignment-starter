package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;

public class CancelledState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        throw new IllegalStateException("Cannot confirm cancelled rental.");
    }
    
    @Override
    public void pickup(Rental rental) {
        throw new IllegalStateException("Cannot pickup vehicle for cancelled rental.");
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        throw new IllegalStateException("Cannot return vehicle for cancelled rental.");
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        throw new IllegalStateException("Cannot complete inspection for cancelled rental.");
    }
    
    @Override
    public void complete(Rental rental) {
        throw new IllegalStateException("Cannot complete cancelled rental.");
    }
    
    @Override
    public void cancel(Rental rental) {
        // Already cancelled
    }
}
