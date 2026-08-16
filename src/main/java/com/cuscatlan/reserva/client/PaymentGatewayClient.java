package com.cuscatlan.reserva.client;

import com.cuscatlan.reserva.config.PaymentGatewayProperties;
import com.cuscatlan.reserva.dto.payment.PaymentValidationRequest;
import com.cuscatlan.reserva.dto.payment.PaymentValidationResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentGatewayClient {

    private final RestClient restClient;

    public PaymentGatewayClient(PaymentGatewayProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) properties.getTimeout().toMillis();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public PaymentValidationResponse validate(PaymentValidationRequest request) {
        return restClient.post()
                .uri("/validate")
                .body(request)
                .retrieve()
                .body(PaymentValidationResponse.class);
    }
}
