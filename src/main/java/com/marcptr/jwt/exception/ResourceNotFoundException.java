package com.marcptr.jwt.exception;

import com.marcptr.jwt.model.enums.ErrorCode;

public class ResourceNotFoundException  extends RuntimeException {
  private final ErrorCode code;
    private final Object details;

    public ResourceNotFoundException(ErrorCode code) {
        this(code, null);
    }

    public ResourceNotFoundException(ErrorCode code, Object details) {
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
