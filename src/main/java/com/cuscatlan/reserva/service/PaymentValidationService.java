package com.cuscatlan.reserva.service;

import com.cuscatlan.reserva.client.PaymentGatewayClient;
import com.cuscatlan.reserva.dto.payment.PaymentValidationRequest;
import com.cuscatlan.reserva.dto.payment.PaymentValidationResponse;
import com.cuscatlan.reserva.entity.Reservation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentValidationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentValidationService.class);

    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentValidationService(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    @CircuitBreaker(name = "paymentValidation", fallbackMethod = "fallback")
    public boolean validate(Reservation reservation) {
        PaymentValidationResponse response = paymentGatewayClient.validate(
                new PaymentValidationRequest(reservation.getId(), reservation.getTotalPrice()));
        return response.approved();
    }

    private boolean fallback(Reservation reservation, Throwable throwable) {
        log.warn("Validación de pago no disponible para la reserva {}, queda en PENDING_PAYMENT: {}",
                reservation.getId(), throwable.getMessage());
        return false;
    }
}
