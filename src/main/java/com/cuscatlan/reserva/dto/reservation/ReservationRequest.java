package com.cuscatlan.reserva.dto.reservation;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record ReservationRequest(

        @NotNull(message = "El espacio es obligatorio")
        Long spaceId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @Future(message = "La fecha de inicio debe ser futura")
        OffsetDateTime startDatetime,

        @NotNull(message = "La fecha de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser futura")
        OffsetDateTime endDatetime
) {

    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isPeriodValid() {
        return startDatetime == null || endDatetime == null || endDatetime.isAfter(startDatetime);
    }
}
