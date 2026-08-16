package com.cuscatlan.reserva.service;

import com.cuscatlan.reserva.dto.report.SpaceOccupancyResponse;
import com.cuscatlan.reserva.entity.Reservation;
import com.cuscatlan.reserva.entity.Space;
import com.cuscatlan.reserva.repository.ReservationRepository;
import com.cuscatlan.reserva.repository.SpaceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;

    public ReportService(SpaceRepository spaceRepository, ReservationRepository reservationRepository) {
        this.spaceRepository = spaceRepository;
        this.reservationRepository = reservationRepository;
    }

    @Cacheable(cacheNames = "occupancyReport", key = "#startDate + '_' + #endDate")
    @Transactional(readOnly = true)
    public List<SpaceOccupancyResponse> occupancyReport(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        OffsetDateTime rangeStart = startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime rangeEnd = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        long totalRangeMinutes = Duration.between(rangeStart, rangeEnd).toMinutes();

        return spaceRepository.findByActiveTrue().stream()
                .map(space -> toOccupancyResponse(space, rangeStart, rangeEnd, totalRangeMinutes))
                .toList();
    }

    private SpaceOccupancyResponse toOccupancyResponse(
            Space space, OffsetDateTime rangeStart, OffsetDateTime rangeEnd, long totalRangeMinutes) {
        List<Reservation> reservations =
                reservationRepository.findActiveBySpaceAndRange(space.getId(), rangeStart, rangeEnd);

        long occupiedMinutes = reservations.stream()
                .mapToLong(reservation -> overlapMinutes(reservation, rangeStart, rangeEnd))
                .sum();

        BigDecimal percentage = totalRangeMinutes == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(occupiedMinutes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalRangeMinutes), 2, RoundingMode.HALF_UP);

        return new SpaceOccupancyResponse(space.getId(), space.getName(), percentage);
    }

    private long overlapMinutes(Reservation reservation, OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        OffsetDateTime start = reservation.getStartDatetime().isAfter(rangeStart)
                ? reservation.getStartDatetime() : rangeStart;
        OffsetDateTime end = reservation.getEndDatetime().isBefore(rangeEnd)
                ? reservation.getEndDatetime() : rangeEnd;
        return Duration.between(start, end).toMinutes();
    }
}
