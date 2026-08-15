package com.cuscatlan.reserva.repository;

import com.cuscatlan.reserva.entity.Reservation;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "space"})
    List<Reservation> findAll();

    @EntityGraph(attributePaths = {"user", "space"})
    List<Reservation> findByUserId(Long userId);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.status <> com.cuscatlan.reserva.entity.ReservationStatus.CANCELLED
              AND r.startDatetime < :end
              AND r.endDatetime > :start
            """)
    boolean existsOverlapping(
            @Param("spaceId") Long spaceId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);
}
