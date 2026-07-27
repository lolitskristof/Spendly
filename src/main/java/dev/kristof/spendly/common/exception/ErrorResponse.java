package dev.kristof.spendly.common.exception;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ErrorResponse {
    private OffsetDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(OffsetDateTime timestamp, Integer status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

}
