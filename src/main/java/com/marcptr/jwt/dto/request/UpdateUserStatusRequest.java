
package com.marcptr.jwt.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

        @NotNull(message = "{error.admin.STATUS_REQUIRED}")
        Boolean enabled

) {}
