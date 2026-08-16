package com.cuscatlan.reserva.event;

import com.cuscatlan.reserva.entity.ReservationStatus;

public record ReservationConfirmationEvent(Long reservationId, String userEmail, ReservationStatus status) {
}
