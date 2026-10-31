
package com.marcptr.jwt.dto.request;

import com.marcptr.jwt.model.enums.Role;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(

        @NotNull(message = "{error.admin.ROLE_REQUIRED}")
        Role role

) {}
