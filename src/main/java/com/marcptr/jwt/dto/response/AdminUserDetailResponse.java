
package com.marcptr.jwt.dto.response;

import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDetailResponse(

        UUID id,
        String username,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt,
        long activeSessions

) {

    public static AdminUserDetailResponse fromEntity(
            User user,
            long activeSessions
    ) {
        return new AdminUserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                activeSessions
        );
    }
}
