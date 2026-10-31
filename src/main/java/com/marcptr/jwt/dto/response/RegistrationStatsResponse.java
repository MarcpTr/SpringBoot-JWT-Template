
package com.marcptr.jwt.dto.response;

import java.util.List;

public record RegistrationStatsResponse(
        String period,
        String from,
        String to,
        long totalRegistrations,
        List<RegistrationData> registrations
) {

    public record RegistrationData(
            String period,
            long registrations
    ) {}
}
