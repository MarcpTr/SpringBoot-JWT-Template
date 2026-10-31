
package com.marcptr.jwt.dto.response;

import com.marcptr.jwt.model.User;
import com.marcptr.jwt.model.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(

        UUID id,
        String username,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt

) {

    public static AdminUserResponse fromEntity(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
