package com.cuscatlan.reserva.exception;

import com.cuscatlan.reserva.entity.ReservationStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(ReservationStatus from, ReservationStatus to) {
        super("No se puede transicionar la reserva de %s a %s".formatted(from, to));
    }
}
