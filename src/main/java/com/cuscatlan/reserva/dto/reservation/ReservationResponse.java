package com.cuscatlan.reserva.dto.reservation;

import com.cuscatlan.reserva.entity.ReservationStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservationResponse(
        Long id,
        Long spaceId,
        String spaceName,
        Long userId,
        String userEmail,
        OffsetDateTime startDatetime,
        OffsetDateTime endDatetime,
        ReservationStatus status,
        BigDecimal totalPrice,
        OffsetDateTime createdAt
) {
}
