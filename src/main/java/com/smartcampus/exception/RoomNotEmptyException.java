package com.smartcampus.exception;

/**
 * Thrown when attempting to delete a Room that still has sensors assigned to it.
 * Maps to HTTP 409 Conflict.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
