
package com.marcptr.jwt.controller;

import java.util.List;
import java.util.UUID;

import com.marcptr.jwt.dto.common.ApiResponse;
import com.marcptr.jwt.dto.request.CreateUserRequest;
import com.marcptr.jwt.dto.request.UpdateUserRequest;
import com.marcptr.jwt.dto.request.UpdateUserRoleRequest;
import com.marcptr.jwt.dto.request.UpdateUserStatusRequest;
import com.marcptr.jwt.dto.response.ActiveSessionResponse;
import com.marcptr.jwt.dto.response.AdminUserDetailResponse;
import com.marcptr.jwt.dto.response.AdminUserResponse;
import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.Role;
import com.marcptr.jwt.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

        private final AdminUserService adminUserService;

        @GetMapping
        public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) Boolean enabled,
                        Pageable pageable) {
                return ResponseEntity.ok(
                                ApiResponse.ok(
                                                adminUserService.getUsers(
                                                                username,
                                                                email,
                                                                role,
                                                                enabled,
                                                                pageable)));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserById(
                        @PathVariable UUID id) {

                return ResponseEntity.ok(
                                ApiResponse.ok(adminUserService.getUserById(id)));
        }

        @PostMapping
        public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
                        @Valid @RequestBody CreateUserRequest request) {

                AdminUserResponse response = adminUserService.createUser(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.ok(response));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<AdminUserResponse>> updateUser(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateUserRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.ok(adminUserService.updateUser(id, request)));
        }

        @PatchMapping("/{id}/role")
        public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateUserRoleRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.ok(adminUserService.updateUserRole(id, request)));
        }

        @PatchMapping("/{id}/status")
        public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserStatus(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateUserStatusRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.ok(adminUserService.updateUserStatus(id, request)));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(
                        @PathVariable UUID id,
                        @AuthenticationPrincipal User currentAdmin) {

                adminUserService.deleteUser(id, currentAdmin.getId());

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{userId}/sessions/{sessionId}")
        public ResponseEntity<Void> revokeUserSession(
                        @PathVariable UUID userId,
                        @PathVariable UUID sessionId) {

                adminUserService.revokeUserSession(userId, sessionId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{userId}/sessions")
        public ResponseEntity<Void> revokeAllUserSessions(
                        @PathVariable UUID userId) {

                adminUserService.revokeAllUserSessions(userId);

                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{userId}/sessions")
        public ResponseEntity<ApiResponse<List<ActiveSessionResponse>>> getUserSessions(
                        @PathVariable UUID userId) {

                return ResponseEntity.ok(
                                ApiResponse.ok(adminUserService.getUserSessions(userId)));
        }
}
