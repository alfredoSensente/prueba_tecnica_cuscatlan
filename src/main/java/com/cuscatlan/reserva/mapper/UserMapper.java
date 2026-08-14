package com.cuscatlan.reserva.mapper;

import com.cuscatlan.reserva.dto.auth.RegisterRequest;
import com.cuscatlan.reserva.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest request);
}
