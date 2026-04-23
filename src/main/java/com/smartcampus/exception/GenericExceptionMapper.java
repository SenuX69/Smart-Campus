package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" exception mapper that handles any unhandled Throwable.
 * Returns HTTP 500 Internal Server Error with a generic message.
 * 
 * This is a critical safety net that ensures internal stack traces and
 * implementation details are never exposed to external consumers,
 * preventing potential information disclosure vulnerabilities.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace on the server for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper", exception);

        ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator.",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
