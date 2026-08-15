package com.cuscatlan.reserva.exception;

import java.time.OffsetDateTime;

public class OverlappingReservationException extends RuntimeException {

    public OverlappingReservationException(Long spaceId, OffsetDateTime start, OffsetDateTime end) {
        super("El espacio %d ya tiene una reserva que se solapa entre %s y %s".formatted(spaceId, start, end));
    }
}
