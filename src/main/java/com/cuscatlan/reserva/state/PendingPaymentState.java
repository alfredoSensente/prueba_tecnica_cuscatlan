package com.cuscatlan.reserva.state;

import com.cuscatlan.reserva.entity.Reservation;
import com.cuscatlan.reserva.entity.ReservationStatus;

public class PendingPaymentState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.PENDING_PAYMENT;
    }

    @Override
    public ReservationState confirm(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return ReservationStates.CONFIRMED;
    }

    @Override
    public ReservationState holdForPayment(Reservation reservation) {
        return this;
    }

    @Override
    public ReservationState cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationStates.CANCELLED;
    }
}
