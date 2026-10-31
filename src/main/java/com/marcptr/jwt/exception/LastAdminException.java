package com.marcptr.jwt.exception;

import com.marcptr.jwt.model.enums.ErrorCode;

public class LastAdminException extends RuntimeException {

    private final ErrorCode code;

    public LastAdminException() {
        super("Cannot remove the last administrator");
        this.code = ErrorCode.LAST_ADMIN_REQUIRED;
    }

    public ErrorCode getCode() {
        return code;
    }

    public Object getDetails() {
        return null;
    }
}