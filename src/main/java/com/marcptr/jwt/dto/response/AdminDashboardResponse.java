
package com.marcptr.jwt.dto.response;

public record AdminDashboardResponse(
        long totalUsers,
        long enabledUsers,
        long disabledUsers,
        long administrators,
        long activeSessions
) {}
