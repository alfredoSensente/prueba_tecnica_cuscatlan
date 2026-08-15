package com.cuscatlan.reserva.exception;

public class SpaceNotFoundException extends RuntimeException {

    public SpaceNotFoundException(Long id) {
        super("No se encontró el espacio con id: " + id);
    }

}
