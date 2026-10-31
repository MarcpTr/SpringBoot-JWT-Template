
package com.marcptr.jwt.service;

import com.marcptr.jwt.dto.response.AdminDashboardResponse;
import com.marcptr.jwt.dto.response.AdminUserStatsResponse;
import com.marcptr.jwt.model.enums.Role;
import com.marcptr.jwt.model.enums.TokenType;
import com.marcptr.jwt.repository.TokenRepository;
import com.marcptr.jwt.repository.UserRepository;
import com.marcptr.jwt.dto.response.AdminSessionStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marcptr.jwt.dto.response.RegistrationStatsResponse;
import com.marcptr.jwt.model.enums.RegistrationPeriod;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

        private final UserRepository userRepository;
        private final TokenRepository tokenRepository;

        @Transactional(readOnly = true)
        public AdminDashboardResponse getDashboard() {

                long totalUsers = userRepository.count();
                long enabledUsers = userRepository.countByEnabledTrue();
                long disabledUsers = userRepository.countByEnabledFalse();
                long administrators = userRepository.countByRole(Role.ROLE_ADMIN);

                long activeSessions = tokenRepository
                                .countByTokenTypeAndExpiredFalseAndRevokedFalse(TokenType.REFRESH);

                return new AdminDashboardResponse(
                                totalUsers,
                                enabledUsers,
                                disabledUsers,
                                administrators,
                                activeSessions);
        }

        @Transactional(readOnly = true)
        public AdminUserStatsResponse getUserStats() {

                long totalUsers = userRepository.count();
                long enabledUsers = userRepository.countByEnabledTrue();
                long disabledUsers = userRepository.countByEnabledFalse();

                long administrators = userRepository.countByRole(Role.ROLE_ADMIN);
                long regularUsers = userRepository.countByRole(Role.ROLE_USER);

                long enabledAdministrators = userRepository.countByRoleAndEnabledTrue(Role.ROLE_ADMIN);

                long enabledRegularUsers = userRepository.countByRoleAndEnabledTrue(Role.ROLE_USER);

                long disabledAdministrators = administrators - enabledAdministrators;
                long disabledRegularUsers = regularUsers - enabledRegularUsers;

                return new AdminUserStatsResponse(
                                totalUsers,
                                enabledUsers,
                                disabledUsers,
                                administrators,
                                regularUsers,
                                enabledAdministrators,
                                disabledAdministrators,
                                enabledRegularUsers,
                                disabledRegularUsers);
        }

        @Transactional(readOnly = true)
        public RegistrationStatsResponse getRegistrationStats(
                        LocalDate from,
                        LocalDate to,
                        RegistrationPeriod period) {

                if (from == null || to == null || period == null || from.isAfter(to)) {
                        throw new IllegalArgumentException("Invalid registration statistics parameters");
                }

                List<RegistrationStatsResponse.RegistrationData> registrations = userRepository.getRegistrationStats(
                                period.name(),
                                from.atStartOfDay(),
                                to.plusDays(1).atStartOfDay()).stream()
                                .map(item -> new RegistrationStatsResponse.RegistrationData(
                                                item.getPeriod(),
                                                item.getRegistrations()))
                                .toList();

                long totalRegistrations = registrations.stream()
                                .mapToLong(RegistrationStatsResponse.RegistrationData::registrations)
                                .sum();

                return new RegistrationStatsResponse(
                                period.name(),
                                from.toString(),
                                to.toString(),
                                totalRegistrations,
                                registrations);
        }

        @Transactional(readOnly = true)
        public AdminSessionStatsResponse getSessionStats() {

                long totalSessions = tokenRepository.countByTokenType(TokenType.REFRESH);

                long activeSessions = tokenRepository.countByTokenTypeAndExpiredFalseAndRevokedFalse(
                                TokenType.REFRESH);

                long inactiveSessions = totalSessions - activeSessions;

                return new AdminSessionStatsResponse(
                                totalSessions,
                                activeSessions,
                                inactiveSessions);
        }

}
