package com.tickethub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** Định dạng lỗi thống nhất trả về cho client. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        String code,
        String message,
        String path,
        Instant timestamp,
        List<FieldError> errors
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path, Instant.now(), List.of());
    }
}
