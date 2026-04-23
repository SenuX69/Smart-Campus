package com.smartcampus.exception;

/**
 * Thrown when a resource references another resource (e.g., roomId) that does not exist.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
