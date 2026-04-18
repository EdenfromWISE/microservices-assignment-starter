package com.carrental.rental.state;

import com.carrental.rental.model.Rental;
import com.carrental.rental.model.RentalState;

public class CompletedState implements RentalState {
    
    @Override
    public void confirm(Rental rental) {
        throw new IllegalStateException("Cannot confirm completed rental.");
    }
    
    @Override
    public void pickup(Rental rental) {
        throw new IllegalStateException("Cannot pickup vehicle for completed rental.");
    }
    
    @Override
    public void returnVehicle(Rental rental) {
        throw new IllegalStateException("Vehicle already returned for completed rental.");
    }
    
    @Override
    public void completeInspection(Rental rental, boolean hasDamage) {
        throw new IllegalStateException("Cannot complete inspection for completed rental.");
    }
    
    @Override
    public void complete(Rental rental) {
        // Already completed
    }
    
    @Override
    public void cancel(Rental rental) {
        throw new IllegalStateException("Cannot cancel completed rental.");
    }
}
