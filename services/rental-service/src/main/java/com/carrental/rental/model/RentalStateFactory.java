package com.carrental.rental.model;

public class RentalStateFactory {
    public static RentalState getState(RentalStatus status) {
        return switch (status) {
            case PENDING -> new com.carrental.rental.state.PendingState();
            case CONFIRMED -> new com.carrental.rental.state.ConfirmedState();
            case IN_PROGRESS -> new com.carrental.rental.state.InProgressState();
            case INSPECTION -> new com.carrental.rental.state.InspectionState();
            case COMPLETED -> new com.carrental.rental.state.CompletedState();
            case PENALTY_DUE -> new com.carrental.rental.state.PenaltyDueState();
            case CANCELLED -> new com.carrental.rental.state.CancelledState();
        };
    }
}
