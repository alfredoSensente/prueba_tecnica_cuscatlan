package com.cuscatlan.reserva.state;

import com.cuscatlan.reserva.entity.Reservation;
import com.cuscatlan.reserva.entity.ReservationStatus;

public class PendingState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.PENDING;
    }

    @Override
    public ReservationState confirm(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return ReservationStates.CONFIRMED;
    }

    @Override
    public ReservationState holdForPayment(Reservation reservation) {
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        return ReservationStates.PENDING_PAYMENT;
    }

    @Override
    public ReservationState cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationStates.CANCELLED;
    }
}
