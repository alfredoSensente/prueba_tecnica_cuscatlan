package com.cuscatlan.reserva.service;

import com.cuscatlan.reserva.dto.reservation.ReservationRequest;
import com.cuscatlan.reserva.dto.reservation.ReservationResponse;
import com.cuscatlan.reserva.entity.Reservation;
import com.cuscatlan.reserva.entity.ReservationStatus;
import com.cuscatlan.reserva.entity.Space;
import com.cuscatlan.reserva.entity.User;
import com.cuscatlan.reserva.exception.OverlappingReservationException;
import com.cuscatlan.reserva.exception.ReservationNotFoundException;
import com.cuscatlan.reserva.exception.SpaceNotFoundException;
import com.cuscatlan.reserva.mapper.ReservationMapper;
import com.cuscatlan.reserva.repository.ReservationRepository;
import com.cuscatlan.reserva.repository.SpaceRepository;
import com.cuscatlan.reserva.repository.UserRepository;
import com.cuscatlan.reserva.state.ReservationStates;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            SpaceRepository spaceRepository,
            UserRepository userRepository,
            ReservationMapper reservationMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, String userEmail) {
        User user = getUserOrThrow(userEmail);
        Space space = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> new SpaceNotFoundException(request.spaceId()));

        if (reservationRepository.existsOverlapping(space.getId(), request.startDatetime(), request.endDatetime())) {
            throw new OverlappingReservationException(space.getId(), request.startDatetime(), request.endDatetime());
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startDatetime(request.startDatetime())
                .endDatetime(request.endDatetime())
                .status(ReservationStatus.PENDING)
                .totalPrice(calculateTotalPrice(space, request.startDatetime(), request.endDatetime()))
                .build();

        reservationRepository.save(reservation);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findVisibleTo(Authentication authentication) {
        if (isAdmin(authentication)) {
            return reservationRepository.findAll().stream()
                    .map(reservationMapper::toResponse)
                    .toList();
        }

        User user = getUserOrThrow(authentication.getName());
        return reservationRepository.findByUserId(user.getId()).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse findByIdVisibleTo(Long id, Authentication authentication) {
        Reservation reservation = getOrThrow(id);
        assertVisible(reservation, authentication);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancel(Long id, Authentication authentication) {
        Reservation reservation = getOrThrow(id);
        assertVisible(reservation, authentication);

        ReservationStates.of(reservation.getStatus()).cancel(reservation);

        return reservationMapper.toResponse(reservation);
    }

    private void assertVisible(Reservation reservation, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }
        if (!reservation.getUser().getEmail().equals(authentication.getName())) {
            throw new AccessDeniedException("No tiene permisos para acceder a esta reserva");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ROLE_ADMIN));
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado: " + email));
    }

    private Reservation getOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private BigDecimal calculateTotalPrice(Space space, OffsetDateTime start, OffsetDateTime end) {
        BigDecimal hours = BigDecimal.valueOf(Duration.between(start, end).toMinutes())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return space.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
