package com.kr.goukon.global.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        int status,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus().value(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(String code, String message, int status) {
        return new ErrorResponse(
                code,
                message,
                status,
                LocalDateTime.now()
        );
    }
}
