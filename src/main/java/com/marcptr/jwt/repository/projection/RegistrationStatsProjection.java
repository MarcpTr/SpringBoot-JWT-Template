package com.marcptr.jwt.repository.projection;

public interface RegistrationStatsProjection {

    String getPeriod();

    long getRegistrations();
}
