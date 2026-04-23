package com.smartcampus.model;

/**
 * Standard error response POJO used by all exception mappers
 * for consistent JSON error output across the API.
 */
public class ErrorResponse {

    private String error;    // Error category, e.g., "Conflict", "Unprocessable Entity"
    private String message;  // Human-readable description of the error
    private int status;      // HTTP status code

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
