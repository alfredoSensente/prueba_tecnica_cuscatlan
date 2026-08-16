package com.cuscatlan.reserva.controller;

import com.cuscatlan.reserva.config.MockPaymentGatewayProperties;
import com.cuscatlan.reserva.dto.payment.PaymentValidationRequest;
import com.cuscatlan.reserva.dto.payment.PaymentValidationResponse;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Simula el proveedor de pago externo del enunciado (potencialmente lento/inestable).
 * PaymentGatewayClient le pega a este endpoint como si fuera un tercero. El failure-rate
 * simula caídas/errores del proveedor (lo que el circuit breaker debe detectar) — no un
 * rechazo de negocio del pago, que es un escenario distinto y no es lo que Resilience4j
 * protege aquí.
 */
@RestController
@RequestMapping("/mock/payment-gateway")
public class MockPaymentGatewayController {

    private final MockPaymentGatewayProperties properties;

    public MockPaymentGatewayController(MockPaymentGatewayProperties properties) {
        this.properties = properties;
    }

    @PostMapping("/validate")
    public PaymentValidationResponse validate(@RequestBody PaymentValidationRequest request) throws InterruptedException {
        if (properties.getDelayMillis() > 0) {
            Thread.sleep(properties.getDelayMillis());
        }

        if (ThreadLocalRandom.current().nextDouble() < properties.getFailureRate()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Proveedor de pago no disponible (simulado)");
        }

        return new PaymentValidationResponse(true);
    }
}
