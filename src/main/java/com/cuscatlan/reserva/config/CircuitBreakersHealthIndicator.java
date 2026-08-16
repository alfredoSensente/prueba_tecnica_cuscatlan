package com.cuscatlan.reserva.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * resilience4j-spring-boot3 2.2.0 trae su propio health indicator, pero su
 * @ConditionalOnClass apunta a org.springframework.boot.actuate.health.HealthIndicator
 * (la ruta de Boot 3.x). Boot 4 movió esa clase a org.springframework.boot.health.contributor,
 * en el módulo nuevo spring-boot-health — la condición nunca matchea y el indicador de
 * resilience4j no se registra. Este es el reemplazo, escrito contra la API nueva de Boot 4.
 *
 * El estado siempre se reporta como UP: un circuito OPEN es una degradación ya manejada
 * por el fallback (la reserva queda en PENDING_PAYMENT), no una falla de la aplicación, así
 * que no debería tumbar el readiness/liveness del pod. El detalle por instancia queda
 * disponible igual para observabilidad.
 */
@Component("circuitBreakersHealthIndicator")
public class CircuitBreakersHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakersHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            builder.withDetail(circuitBreaker.getName(), circuitBreaker.getState().name());
        }
        return builder.build();
    }
}
