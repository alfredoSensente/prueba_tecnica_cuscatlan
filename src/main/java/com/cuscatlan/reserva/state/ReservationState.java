package com.cuscatlan.reserva.state;

import com.cuscatlan.reserva.entity.Reservation;
import com.cuscatlan.reserva.entity.ReservationStatus;
import com.cuscatlan.reserva.exception.InvalidStateTransitionException;

public interface ReservationState {

    ReservationStatus status();

    default ReservationState confirm(Reservation reservation) {
        throw invalidTransition(ReservationStatus.CONFIRMED);
    }

    default ReservationState holdForPayment(Reservation reservation) {
        throw invalidTransition(ReservationStatus.PENDING_PAYMENT);
    }

    default ReservationState cancel(Reservation reservation) {
        throw invalidTransition(ReservationStatus.CANCELLED);
    }

    default ReservationState complete(Reservation reservation) {
        throw invalidTransition(ReservationStatus.COMPLETED);
    }

    private InvalidStateTransitionException invalidTransition(ReservationStatus target) {
        return new InvalidStateTransitionException(status(), target);
    }
}
