package com.cuscatlan.reserva.dto.auth;

import com.cuscatlan.reserva.entity.Role;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        Role role
) {

    public static AuthResponse bearer(String token, String email, Role role) {
        return new AuthResponse(token, "Bearer", email, role);
    }
}
