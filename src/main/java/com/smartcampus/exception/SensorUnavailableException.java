package com.smartcampus.exception;

/**
 * Thrown when attempting to post a reading to a sensor that is in "MAINTENANCE" status.
 * Maps to HTTP 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
