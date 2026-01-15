package com.gatcha.combat.dto.response;

import java.time.LocalDateTime;

/**
 * DTO pour les reponses d'erreur
 */
public class ErrorResponseDto {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;

    public ErrorResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDto(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public static ErrorResponseDto badRequest(String message) {
        return new ErrorResponseDto(400, "Bad Request", message);
    }

    public static ErrorResponseDto notFound(String message) {
        return new ErrorResponseDto(404, "Not Found", message);
    }

    public static ErrorResponseDto internalError(String message) {
        return new ErrorResponseDto(500, "Internal Server Error", message);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
