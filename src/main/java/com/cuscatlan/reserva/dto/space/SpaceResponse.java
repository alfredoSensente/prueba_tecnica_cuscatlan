package com.cuscatlan.reserva.dto.space;

import com.cuscatlan.reserva.entity.SpaceType;
import java.math.BigDecimal;

public record SpaceResponse(
        Long id,
        String name,
        SpaceType type,
        Integer capacity,
        String location,
        BigDecimal hourlyRate,
        Boolean active
) {
}
