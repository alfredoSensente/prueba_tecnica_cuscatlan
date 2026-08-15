package com.cuscatlan.reserva.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long id) {
        super("No se encontró la reserva con id: " + id);
    }
}
