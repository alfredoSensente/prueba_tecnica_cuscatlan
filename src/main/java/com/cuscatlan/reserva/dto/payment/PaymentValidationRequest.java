package com.cuscatlan.reserva.dto.payment;

import java.math.BigDecimal;

public record PaymentValidationRequest(Long reservationId, BigDecimal amount) {
}
