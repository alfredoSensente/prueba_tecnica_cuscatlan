package com.cuscatlan.reserva.state;

import com.cuscatlan.reserva.entity.ReservationStatus;

public class CompletedState implements ReservationState {

    @Override
    public ReservationStatus status() {
        return ReservationStatus.COMPLETED;
    }
}
