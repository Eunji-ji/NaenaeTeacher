package com.naenae.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        List<String> errors,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse of(String message, List<String> errors) {
        return new ErrorResponse(false, message, errors, LocalDateTime.now());
    }
}
