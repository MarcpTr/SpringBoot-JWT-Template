package com.marcptr.jwt.dto.response;

public record AdminUserStatsResponse(
        long totalUsers,
        long enabledUsers,
        long disabledUsers,
        long administrators,
        long regularUsers,
        long enabledAdministrators,
        long disabledAdministrators,
        long enabledRegularUsers,
        long disabledRegularUsers
) {}
