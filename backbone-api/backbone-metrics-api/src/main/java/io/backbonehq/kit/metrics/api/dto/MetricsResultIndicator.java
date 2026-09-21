package io.backbonehq.kit.metrics.api.dto;

/**
 * Marker interface for return types that should be automatically instrumented with metrics.
 * Methods annotated with {@code @ServiceMetrics} must return a type that implements this
 * interface for metrics to be automatically recorded.
 *
 * <p>The metrics interceptor will cast the method's return value to this interface and pass
 * it to the appropriate metrics handler to record success/failure metrics based on the result.
 *
 * <p>Example implementation:
 * <pre>
 * {@code
 * public record AuthResponse(boolean success, String accessToken, String errorMessage)
 *     implements MetricsResultIndicator
 * {
 *     // Implementation
 * }
 * }
 * </pre>
 */
public interface MetricsResultIndicator
{
    /**
     * Indicates whether the operation was successful.
     *
     * @return true if the operation succeeded, false otherwise
     */
    boolean success();

    /**
     * Returns an error message if the operation failed, or null if it succeeded.
     *
     * @return the error message, or null if the operation was successful
     */
    String errorMessage();
}
