package com.marcptr.jwt.dto.common;

import com.marcptr.jwt.model.enums.ErrorCode;

public record ApiError<T> (
    ErrorCode code,
    String message,
    T details
){}
