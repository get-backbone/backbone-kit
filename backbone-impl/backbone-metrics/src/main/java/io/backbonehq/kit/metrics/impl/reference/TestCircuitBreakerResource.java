package io.backbonehq.kit.metrics.impl.reference;

import io.backbonehq.kit.metrics.api.faulttolerance.CircuitBreakerMetrics;
import io.quarkus.arc.profile.IfBuildProfile;
import io.smallrye.faulttolerance.api.CircuitBreakerName;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

/**
 * Test resource for triggering circuit breaker state changes.
 * Only available in dev profile for testing purposes.
 *
 * <p>This is a lightweight test endpoint with no external dependencies.
 * It provides a controlled way to test circuit breaker behavior by
 * triggering simulated failures. The circuit breaker is configured with:
 * requestVolumeThreshold=8, failureRatio=0.5, delay=1000ms
 *
 * <p>This resource is part of the metrics library and will be available
 * in any service that includes the metrics library when running in dev profile.
 */
@Path("/test/circuit-breaker")
@IfBuildProfile("dev")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@CircuitBreakerMetrics
public final class TestCircuitBreakerResource
{
    private static final Logger LOGGER = Logger.getLogger(TestCircuitBreakerResource.class);

    /**
     * Triggers a circuit breaker by simulating success or failure.
     * This is a lightweight test method with no external dependencies.
     *
     * @param shouldFail If true, throws an exception to trigger circuit breaker failure.
     *                   If false, returns successfully.
     * @param count      Number of times to call the method (default: 1)
     * @return Response indicating success or failure
     */
    @POST
    @Path("/trigger")
    @Timeout(1000)
    @Retry(maxRetries = 2, delay = 150, jitter = 50)
    @CircuitBreaker(delay = 1000)
    @CircuitBreakerName("io.backbonehq.metrics.presentation.rest.TestCircuitBreakerResource#triggerCircuitBreaker")
    public Response triggerCircuitBreaker(@QueryParam("fail") final Boolean shouldFail, @QueryParam("count") final Integer count)
    {
        final boolean fail = Boolean.TRUE.equals(shouldFail);
        final int iterations = count != null && count > 0 ? count : 1;

        LOGGER.infof("Triggering circuit breaker test: fail=%s, count=%d", fail, iterations);

        // Don't use a loop - each HTTP request should be a single circuit breaker invocation
        // The circuit breaker tracks state across multiple HTTP requests, not within a single request
        if (fail)
        {
            // Throw a test exception to trigger circuit breaker failure
            // The circuit breaker will catch this and record it as a failure
            // TestExceptionMapper will handle this gracefully without logging stack traces
            throw new TestException("Simulated failure for circuit breaker testing");
        }

        // Simple success - no external dependencies
        return Response.ok()
            .entity(Map.of(
                "message", "Circuit breaker test completed",
                "iterations", iterations,
                "successCount", 1,
                "failureCount", 0,
                "circuitBreakerMethod", "TestCircuitBreakerResource.triggerCircuitBreaker"
            ))
            .build();
    }

    /**
     * Exception thrown to simulate circuit breaker failures.
     * This exception is handled by {@link TestExceptionMapper} to return
     * a graceful response without logging stack traces.
     */
    public static final class TestException extends RuntimeException
    {
        public TestException(final String message)
        {
            super(message);
        }
    }

    /**
     * JAX-RS exception mapper that converts {@link TestException} to a 500 Internal Server Error HTTP response.
     * This prevents test exception stack traces from being logged as errors and provides a graceful response.
     */
    @Provider
    public static final class TestExceptionMapper implements ExceptionMapper<TestException>
    {
        private static final Logger LOGGER = Logger.getLogger(TestExceptionMapper.class);

        @Override
        public Response toResponse(final TestException exception)
        {
            LOGGER.debugf("Circuit breaker test exception: %s", exception.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "message", exception.getMessage(),
                    "type", "test_exception"
                ))
                .build();
        }
    }
}
