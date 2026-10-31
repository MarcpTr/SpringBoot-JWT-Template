
package com.marcptr.jwt.dto.response;

public record AdminSessionStatsResponse(
        long totalSessions,
        long activeSessions,
        long inactiveSessions
) {}
