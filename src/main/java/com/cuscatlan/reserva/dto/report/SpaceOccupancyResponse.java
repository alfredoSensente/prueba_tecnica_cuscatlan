package com.cuscatlan.reserva.dto.report;

import java.math.BigDecimal;

public record SpaceOccupancyResponse(Long spaceId, String spaceName, BigDecimal occupancyPercentage) {
}
