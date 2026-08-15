package com.cuscatlan.reserva.dto.space;

import com.cuscatlan.reserva.entity.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record SpaceRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @NotNull(message = "El tipo de espacio es obligatorio")
        SpaceType type,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor a 0")
        Integer capacity,

        @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres")
        String location,

        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "0.0", message = "La tarifa por hora no puede ser negativa")
        BigDecimal hourlyRate
) {
}
