package com.marcptr.jwt.dto.response;

import java.util.UUID;

public record AuthResponse(
    User user,
    String accessToken,
    String refreshToken
) {
    public record User(
        UUID  id,
        String email,
        String username
    ) {}
}