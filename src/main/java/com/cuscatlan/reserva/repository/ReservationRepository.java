package com.cuscatlan.reserva.repository;

import com.cuscatlan.reserva.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
