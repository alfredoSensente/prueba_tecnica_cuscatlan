package com.cuscatlan.reserva.state;

import com.cuscatlan.reserva.entity.ReservationStatus;

public final class ReservationStates {

    public static final ReservationState PENDING = new PendingState();
    public static final ReservationState PENDING_PAYMENT = new PendingPaymentState();
    public static final ReservationState CONFIRMED = new ConfirmedState();
    public static final ReservationState COMPLETED = new CompletedState();
    public static final ReservationState CANCELLED = new CancelledState();

    private ReservationStates() {
    }

    public static ReservationState of(ReservationStatus status) {
        return switch (status) {
            case PENDING -> PENDING;
            case PENDING_PAYMENT -> PENDING_PAYMENT;
            case CONFIRMED -> CONFIRMED;
            case COMPLETED -> COMPLETED;
            case CANCELLED -> CANCELLED;
        };
    }
}
