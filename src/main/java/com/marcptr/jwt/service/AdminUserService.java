
package com.marcptr.jwt.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marcptr.jwt.dto.request.UpdateUserStatusRequest;
import com.marcptr.jwt.dto.response.AdminUserResponse;
import com.marcptr.jwt.model.enums.Role;
import com.marcptr.jwt.repository.UserRepository;
import com.marcptr.jwt.dto.response.AdminUserDetailResponse;
import com.marcptr.jwt.exception.ResourceNotFoundException;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.ErrorCode;
import com.marcptr.jwt.model.enums.TokenType;
import com.marcptr.jwt.dto.request.UpdateUserRoleRequest;
import com.marcptr.jwt.exception.LastAdminException;
import com.marcptr.jwt.model.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;
import com.marcptr.jwt.dto.response.ActiveSessionResponse;
import java.util.List;
import com.marcptr.jwt.repository.TokenRepository;
import com.marcptr.jwt.dto.request.CreateUserRequest;
import com.marcptr.jwt.exception.ResourceAlreadyExistsException;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.ErrorCode;
import com.marcptr.jwt.dto.request.UpdateUserRequest;
import com.marcptr.jwt.exception.ResourceAlreadyExistsException;
import com.marcptr.jwt.exception.ResourceNotFoundException;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.ErrorCode;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AdminUserService {

        private final UserRepository userRepository;
        private final TokenRepository tokenRepository;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
        private final TokenService tokenService;

        @Transactional(readOnly = true)
        public Page<AdminUserResponse> getUsers(
                        String username,
                        String email,
                        Role role,
                        Boolean enabled,
                        Pageable pageable) {

                username = normalizeFilter(username);
                email = normalizeFilter(email);

                return userRepository.findAllWithFilters(
                                username,
                                email,
                                role,
                                enabled,
                                pageable).map(AdminUserResponse::fromEntity);
        }

        private String normalizeFilter(String value) {
                return (value == null || value.isBlank()) ? null : value.trim();
        }

        @Transactional(readOnly = true)
        public AdminUserDetailResponse getUserById(UUID id) {

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ErrorCode.USER_NOT_FOUND));

                long activeSessions = tokenRepository
                                .countByUser_IdAndTokenTypeAndExpiredFalseAndRevokedFalse(
                                                user.getId(),
                                                TokenType.REFRESH);

                return AdminUserDetailResponse.fromEntity(
                                user,
                                activeSessions);
        }

        @Transactional
        public AdminUserResponse createUser(CreateUserRequest request) {

                String username = request.username().trim();
                String email = request.email().trim().toLowerCase();

                if (userService.existsByUsername(username)) {
                        throw new ResourceAlreadyExistsException(
                                        ErrorCode.USERNAME_ALREADY_EXISTS);
                }

                if (userService.existsByEmail(email)) {
                        throw new ResourceAlreadyExistsException(
                                        ErrorCode.EMAIL_ALREADY_EXISTS);
                }

                User user = User.builder()
                                .username(username)
                                .email(email)
                                .password(passwordEncoder.encode(request.password()))
                                .role(request.role())
                                .enabled(true)
                                .build();

                User savedUser = userRepository.save(user);

                return AdminUserResponse.fromEntity(savedUser);
        }

        @Transactional
        public AdminUserResponse updateUser(
                        UUID id,
                        UpdateUserRequest request) {

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ErrorCode.USER_NOT_FOUND));

                String username = request.username().trim();
                String email = request.email().trim().toLowerCase();

                if (userRepository.existsByUsernameAndIdNot(username, id)) {
                        throw new ResourceAlreadyExistsException(
                                        ErrorCode.USERNAME_ALREADY_EXISTS);
                }

                if (userRepository.existsByEmailAndIdNot(email, id)) {
                        throw new ResourceAlreadyExistsException(
                                        ErrorCode.EMAIL_ALREADY_EXISTS);
                }

                user.setUsername(username);
                user.setEmail(email);

                User updatedUser = userRepository.save(user);

                return AdminUserResponse.fromEntity(updatedUser);
        }

        @Transactional
        public AdminUserResponse updateUserRole(
                        UUID id,
                        UpdateUserRoleRequest request) {

                List<User> administrators = userRepository.findAllByRoleForUpdate(Role.ROLE_ADMIN);

                User user = userRepository.findByIdForUpdate(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ErrorCode.USER_NOT_FOUND));

                Role newRole = request.role();

                if (user.getRole() == newRole) {
                        return AdminUserResponse.fromEntity(user);
                }

                if (user.getRole() == Role.ROLE_ADMIN
                                && newRole != Role.ROLE_ADMIN
                                && user.isEnabled()) {

                        long enabledAdministrators = administrators.stream()
                                        .filter(User::isEnabled)
                                        .count();

                        if (enabledAdministrators <= 1) {
                                throw new LastAdminException();
                        }
                }

                user.setRole(newRole);

                User updatedUser = userRepository.save(user);

                tokenService.revokeAllUserTokens(updatedUser);

                return AdminUserResponse.fromEntity(updatedUser);
        }

        @Transactional
        public AdminUserResponse updateUserStatus(
                        UUID id,
                        UpdateUserStatusRequest request) {

                List<User> administrators = userRepository.findAllByRoleForUpdate(Role.ROLE_ADMIN);

                User user = userRepository.findByIdForUpdate(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ErrorCode.USER_NOT_FOUND));

                boolean newStatus = request.enabled();

                if (user.isEnabled() == newStatus) {
                        return AdminUserResponse.fromEntity(user);
                }

                if (user.getRole() == Role.ROLE_ADMIN
                                && user.isEnabled()
                                && !newStatus) {

                        long enabledAdministrators = administrators.stream()
                                        .filter(User::isEnabled)
                                        .count();

                        if (enabledAdministrators <= 1) {
                                throw new LastAdminException();
                        }
                }

                user.setEnabled(newStatus);

                User updatedUser = userRepository.save(user);

                if (!newStatus) {
                        tokenService.revokeAllUserTokens(updatedUser);
                }

                return AdminUserResponse.fromEntity(updatedUser);
        }

        @Transactional
        public void deleteUser(UUID id, UUID currentAdminId) {

                if (id.equals(currentAdminId)) {
                        throw new AccessDeniedException(
                                        "Administrators cannot delete their own account");
                }

                List<User> administrators = userRepository.findAllByRoleForUpdate(Role.ROLE_ADMIN);

                User user = userRepository.findByIdForUpdate(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ErrorCode.USER_NOT_FOUND));

                if (user.getRole() == Role.ROLE_ADMIN && user.isEnabled()) {

                        long enabledAdministrators = administrators.stream()
                                        .filter(User::isEnabled)
                                        .count();

                        if (enabledAdministrators <= 1) {
                                throw new LastAdminException();
                        }
                }

                tokenService.revokeAllUserTokens(user);

                tokenRepository.deleteAllByUser_Id(user.getId());

                userRepository.delete(user);
        }

        public List<ActiveSessionResponse> getUserSessions(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

                return tokenService.getActiveSessions(user);
        }

        public void revokeUserSession(UUID userId, UUID sessionId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

                tokenService.revokeSessionForUser(user.getId(), sessionId);
        }

        public void revokeAllUserSessions(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

                tokenService.revokeAllUserTokens(user);
        }

}
