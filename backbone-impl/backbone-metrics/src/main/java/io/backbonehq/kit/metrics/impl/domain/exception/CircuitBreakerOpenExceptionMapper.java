package io.backbonehq.kit.metrics.impl.domain.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;

/**
 * JAX-RS exception mapper that converts {@link CircuitBreakerOpenException} to a 503 Service Unavailable HTTP response.
 * This prevents circuit breaker open exceptions from being logged as errors and provides a graceful response.
 */
@Provider
public final class CircuitBreakerOpenExceptionMapper implements ExceptionMapper<CircuitBreakerOpenException>
{
    @Override
    public Response toResponse(final CircuitBreakerOpenException exception)
    {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .entity(Map.of(
                "message", "Circuit breaker is open",
                "circuitBreaker", exception.getMessage(),
                "status", "open"
            ))
            .build();
    }
}
