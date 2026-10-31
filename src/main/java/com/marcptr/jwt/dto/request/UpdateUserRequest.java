
package com.marcptr.jwt.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank(message = "{error.register.USERNAME_REQUIRED}")
        @Size(min = 2, max = 10, message = "{error.register.USERNAME_FORMAT}")
        @Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "{error.register.USERNAME_FORMAT}"
        )
        String username,

        @NotBlank(message = "{error.register.EMAIL_REQUIRED}")
        @Email(message = "{error.register.EMAIL_FORMAT}")
        String email

) {}
