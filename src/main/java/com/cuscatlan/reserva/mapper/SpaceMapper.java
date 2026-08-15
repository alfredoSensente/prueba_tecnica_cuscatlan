package com.cuscatlan.reserva.mapper;

import com.cuscatlan.reserva.dto.space.SpaceRequest;
import com.cuscatlan.reserva.dto.space.SpaceResponse;
import com.cuscatlan.reserva.entity.Space;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Space toEntity(SpaceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(SpaceRequest request, @MappingTarget Space space);

    SpaceResponse toResponse(Space space);
}
