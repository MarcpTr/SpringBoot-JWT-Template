package com.marcptr.jwt.exception;

import com.marcptr.jwt.model.enums.ErrorCode;

public class InvalidCredentialsException extends RuntimeException {
    private final ErrorCode code;
    private final Object details;

    public InvalidCredentialsException(ErrorCode code) {
        this(code, null);
    }

    public InvalidCredentialsException(ErrorCode code, Object details) {
        this.code = code;
        this.details = details;
    }

    public ErrorCode getCode() {
        return code;
    }

    public Object getDetails() {
        return details;
    }
}
