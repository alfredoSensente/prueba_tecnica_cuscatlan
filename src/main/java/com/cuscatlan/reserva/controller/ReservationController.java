package com.cuscatlan.reserva.controller;

import com.cuscatlan.reserva.dto.reservation.ReservationRequest;
import com.cuscatlan.reserva.dto.reservation.ReservationResponse;
import com.cuscatlan.reserva.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationRequest request, Authentication authentication) {
        ReservationResponse response = reservationService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(reservationService.findVisibleTo(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(reservationService.findByIdVisibleTo(id, authentication));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(reservationService.cancel(id, authentication));
    }
}
