package com.marcptr.jwt.dto.response;

public record RefreshResponse(
   String accessToken,
   String refreshToken
) {}