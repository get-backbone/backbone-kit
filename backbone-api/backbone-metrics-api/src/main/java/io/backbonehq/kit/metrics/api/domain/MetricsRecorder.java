package io.backbonehq.kit.metrics.api.domain;

import io.backbonehq.kit.metrics.api.dto.MetricsResultIndicator;
import jakarta.interceptor.InvocationContext;

/**
 * Interface for type-safe metric recorder.
 * Each metric recorder class implements specific logic for recording metrics
 * based on the method's execution result.
 *
 * <p>Implementations should be stateless and thread-safe, as they may be
 * instantiated once and reused across multiple method invocations.
 */
public interface MetricsRecorder
{
    /**
     * Records metrics based on the method's execution result.
     *
     * @param context                The invocation context containing method and target information.
     *                               Available for future use if recorders need access to method parameters,
     *                               annotations,
     *                               etc.
     * @param metricsResultIndicator The success/failure result returned by the intercepted method.
     *                               May be null for void methods; recorders should extract data from request context
     *                               or other sources in such cases.
     */
    void recordMetrics(InvocationContext context, MetricsResultIndicator metricsResultIndicator);

    /**
     * Records metrics for an exception that occurred during method execution.
     *
     * @param context   The invocation context containing method and target information.
     *                  Available for future use if recorders need access to method parameters, annotations, etc.
     * @param exception The exception that was thrown
     */
    void recordException(InvocationContext context, Exception exception);
}
