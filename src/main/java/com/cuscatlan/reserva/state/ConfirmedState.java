package com.cuscatlan.reserva.state;

import com.cuscatlan.reserva.entity.Reservation;
import com.cuscatlan.reserva.entity.ReservationStatus;

public class ConfirmedState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.CONFIRMED;
    }

    @Override
    public ReservationState cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationStates.CANCELLED;
    }

    @Override
    public ReservationState complete(Reservation reservation) {
        reservation.setStatus(ReservationStatus.COMPLETED);
        return ReservationStates.COMPLETED;
    }
}
