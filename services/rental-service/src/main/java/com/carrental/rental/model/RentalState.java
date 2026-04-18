package com.carrental.rental.model;

public interface RentalState {
    void confirm(Rental rental);
    void pickup(Rental rental);
    void returnVehicle(Rental rental);
    void completeInspection(Rental rental, boolean hasDamage);
    void complete(Rental rental);
    void cancel(Rental rental);
}
