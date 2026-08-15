package com.cuscatlan.reserva.mapper;

import com.cuscatlan.reserva.dto.reservation.ReservationResponse;
import com.cuscatlan.reserva.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "spaceId", source = "space.id")
    @Mapping(target = "spaceName", source = "space.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    ReservationResponse toResponse(Reservation reservation);
}
