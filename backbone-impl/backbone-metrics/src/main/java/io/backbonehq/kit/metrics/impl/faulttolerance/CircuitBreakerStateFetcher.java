package io.backbonehq.kit.metrics.impl.faulttolerance;

import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Safely fetches circuit breaker state from CircuitBreakerMaintenance.
 *
 * <p>Circuit breakers are only accessible through {@link CircuitBreakerMaintenance} when
 * they are explicitly named using {@link io.smallrye.faulttolerance.api.CircuitBreakerName}. If the circuit breaker
 * is not found, this class throws an exception to fail fast.
 */
@ApplicationScoped
public final class CircuitBreakerStateFetcher
{
    private final CircuitBreakerMaintenance circuitBreakerMaintenance;

    @Inject
    public CircuitBreakerStateFetcher(CircuitBreakerMaintenance circuitBreakerMaintenance)
    { this.circuitBreakerMaintenance = circuitBreakerMaintenance; }

    /**
     * Gets the current circuit breaker state using SmallRye Fault Tolerance API.
     *
     * @param circuitName The circuit breaker name
     * @return The current state, or null if the circuit breaker is not accessible
     * @throws IllegalStateException if the circuit breaker is not accessible through CircuitBreakerMaintenance
     */
    public CircuitBreakerState getCurrentState(final String circuitName)
    {
        try
        {
            return circuitBreakerMaintenance.currentState(circuitName);
        }
        catch (final Exception e)
        {
            handleStateAccessException(circuitName, e);
            return null; // unreachable but required by compiler
        }
    }

    private void handleStateAccessException(final String circuitName, final Exception e)
    {
        // Circuit breakers created via annotations (without @CircuitBreakerName) are not
        // registered in CircuitBreakerMaintenance. Fail fast with a clear error message.
        if (e.getMessage() != null && e.getMessage().contains("doesn't exist"))
        {
            throw new IllegalStateException(String.format(
                "Circuit breaker '%s' is not accessible through CircuitBreakerMaintenance. " + "Circuit breakers must be explicitly named using @CircuitBreakerName. Original error: %s",
                circuitName, e.getMessage()), e);
        }
        throw new IllegalStateException(String.format("Could not get circuit breaker state for '%s': %s", circuitName, e.getMessage()), e);
    }
}
