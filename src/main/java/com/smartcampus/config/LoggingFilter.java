package com.smartcampus.config;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter implementing both ContainerRequestFilter and ContainerResponseFilter
 * for cross-cutting API logging concerns.
 * 
 * Logs incoming request details (method, URI) and outgoing response details
 * (status code, response time) without requiring manual logging in each resource method.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
    private static final String START_TIME_PROPERTY = "request-start-time";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Record the start time for response time calculation
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());

        LOGGER.info(String.format("[REQUEST] %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().toString()
        ));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        long startTime = (long) requestContext.getProperty(START_TIME_PROPERTY);
        long duration = System.currentTimeMillis() - startTime;

        LOGGER.info(String.format("[RESPONSE] %s %s — Status: %d — Duration: %dms",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().toString(),
                responseContext.getStatus(),
                duration
        ));
    }
}
