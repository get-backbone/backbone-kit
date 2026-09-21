package io.backbonehq.kit.metrics.impl.domain.recorder;

import io.backbonehq.kit.metrics.api.domain.MetricsRecorder;
import io.backbonehq.kit.metrics.api.dto.MetricsResultIndicator;
import io.backbonehq.kit.metrics.impl.infrastructure.throttle.RateLimitKeyParser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.util.Objects;
import java.util.stream.Stream;
import org.jboss.logging.Logger;

/**
 * Metrics recorder for rate limiting operations.
 * Records rate limit metrics based on data stored in request context properties.
 *
 * <p>This recorder extracts rate limit key and status from request context properties
 * set by the rate limiting filter ({@code RateLimitingFilter} in the security library).
 * The recorder is invoked via the {@code ServiceMetricsInterceptor}
 * for void filter methods.
 *
 * <p>Unlike other recorders, this recorder does not rely on a return value
 * (since filters return void), but instead reads data from request context properties
 * that are set before the interceptor is invoked. The filter stores the rate limit key
 * and status as separate properties to avoid cross-library dependencies.
 */
@ApplicationScoped
public final class ThrottleMetricsRecorder implements MetricsRecorder
{
    private static final Logger LOGGER = Logger.getLogger(ThrottleMetricsRecorder.class);

    @Inject
    MeterRegistry meterRegistry;

    /**
     * Records rate limiting metrics based on data stored in request context properties.
     *
     * <p>For rate limiting filters (which return void), the metricsResultIndicator will be null.
     * This method extracts rate limit key and status from request context properties set by
     * the rate limiting filter ({@code RateLimitingFilter} in the security library).
     *
     * <p>Extracts the ContainerRequestContext from the method parameters via InvocationContext,
     * since filter methods receive the request context as their first parameter.
     *
     * @param context                The invocation context containing method parameters
     * @param metricsResultIndicator Always null for void filter methods
     */
    @Override
    public void recordMetrics(final InvocationContext context, final MetricsResultIndicator metricsResultIndicator)
    {
        // For rate limiting, metricsResultIndicator will be null (void method)
        // Extract data from request context properties instead
        // The request context is passed as the first parameter to filter methods
        final ContainerRequestContext requestContext = extractRequestContext(context);
        if (requestContext != null)
        {
            final String key = (String) requestContext.getProperty(RateLimitKeyParser.RATE_LIMIT_KEY_PROPERTY);
            final Boolean allowed = (Boolean) requestContext.getProperty(
                RateLimitKeyParser.RATE_LIMIT_STATUS_ALLOWED_PROPERTY);
            final Long limit = (Long) requestContext.getProperty(RateLimitKeyParser.RATE_LIMIT_STATUS_LIMIT_PROPERTY);
            final Long remaining = (Long) requestContext.getProperty(
                RateLimitKeyParser.RATE_LIMIT_STATUS_REMAINING_PROPERTY);

            if (Stream.of(key, allowed, limit, remaining).allMatch(Objects::nonNull))
            {
                final String keyType = RateLimitKeyParser.extractKeyType(key);
                final String identifier = RateLimitKeyParser.extractIdentifier(key);

                recordRateLimitCheck(keyType, identifier, allowed, limit, remaining);
                recordRateLimitUtilization(keyType, limit, remaining);
            }
            else
            {
                LOGGER.warnf(
                    "ThrottleMetricsRecorder: Missing rate limit properties - key=%s, allowed=%s, limit=%s, remaining=%s",
                    key, allowed, limit, remaining);
            }
        }
    }

    @Override
    public void recordException(final InvocationContext context, final Exception exception)
    {
        // An exception in rate limiting means the throttling mechanism itself failed
        // (e.g., Redis connection issue, rate limiter bug, etc.)
        // This is different from a rate limit violation - it's a failure of the rate limiting system
        recordRateLimitFailure(exception);
    }

    /**
     * Records a rate limiting failure.
     * This indicates the rate limiting mechanism itself failed, not that a limit was exceeded.
     *
     * @param exception The exception that occurred during rate limiting
     */
    private void recordRateLimitFailure(final Exception exception)
    {
        final String exceptionType = exception.getClass().getSimpleName();
        Counter.builder("rate.limit.failures")
            .tag("exception_type", exceptionType)
            .description("Rate limiting mechanism failures - indicates the throttling system itself failed")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Records a rate limit check result.
     * Tracks both allowed and blocked requests for generic overview metrics.
     *
     * @param keyType    The type of rate limit key (e.g., "user", "service", "ip", "auth")
     * @param identifier The specific identifier (username, serviceId, IP address, or "unidentified")
     * @param allowed    Whether the request was allowed
     * @param limit      The rate limit capacity
     * @param remaining  The remaining tokens in the bucket
     */
    private void recordRateLimitCheck(final String keyType, final String identifier, final boolean allowed, final long limit, final long remaining)
    {
        Counter.builder("rate.limit.requests")
            .tag("key_type", keyType)
            .tag("status", allowed ? "allowed" : "blocked")
            .description("Rate limit check results - tracks allowed and blocked requests")
            .register(meterRegistry)
            .increment();

        if (!allowed)
        {
            recordRateLimitViolation(keyType, identifier, limit, remaining);
        }
    }

    /**
     * Records a rate limit violation with specific identifier details.
     * This metric is specifically for violations and includes the identifier for alerting.
     *
     * @param keyType    The type of rate limit key (e.g., "user", "service", "ip", "auth")
     * @param identifier The specific identifier that violated the limit (username, serviceId, IP address)
     * @param limit      The rate limit capacity that was exceeded
     * @param remaining  The remaining tokens (should be 0 or negative when blocked)
     */
    private void recordRateLimitViolation(final String keyType, final String identifier, final long limit, final long remaining)
    {
        LOGGER.debugf("Recording rate limit violation - keyType=%s, identifier=%s, limit=%d, remaining=%d",
            keyType, sanitizeIdentifier(identifier), limit, remaining);

        Counter.builder("rate.limit.violations")
            .tag("key_type", keyType)
            .tag("identifier", sanitizeIdentifier(identifier))
            .description("Rate limit violations - tracks specific IPs or user IDs that exceeded limits")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Records rate limit utilization percentage.
     * This provides a generic overview of how close entities are to their limits.
     * Utilization is calculated as (limit - remaining) / limit, giving a percentage of capacity used.
     * This metric helps identify if limits are reasonable or being approached.
     *
     * @param keyType   The type of rate limit key (e.g., "user", "service", "ip", "auth")
     * @param limit     The rate limit capacity
     * @param remaining The remaining tokens in the bucket
     */
    private void recordRateLimitUtilization(final String keyType, final long limit, final long remaining)
    {
        if (limit > 0)
        {
            final double utilization = ((double) (limit - remaining)) / limit * 100.0;

            DistributionSummary.builder("rate.limit.utilization")
                .tag("key_type", keyType)
                .description(
                    "Rate limit utilization percentage (0-100) - indicates how close entities are to their limits")
                .register(meterRegistry)
                .record(utilization);
        }
    }

    /**
     * Sanitizes identifier for use in metrics tags.
     * Removes or truncates identifiers that might be too long or contain special characters.
     * IP addresses and usernames are kept as-is, but very long identifiers are truncated.
     *
     * @param identifier The identifier to sanitize
     * @return Sanitized identifier safe for use in metrics tags
     */
    private String sanitizeIdentifier(final String identifier)
    {
        if (identifier == null)
        {
            return "unknown";
        }

        // Truncate very long identifiers to prevent metric cardinality explosion
        final int maxLength = 100;
        if (identifier.length() > maxLength)
        {
            return identifier.substring(0, maxLength) + "...";
        }

        return identifier;
    }

    /**
     * Extracts ContainerRequestContext from the method parameters via InvocationContext.
     * Filter methods receive the request context as their first (and typically only) parameter.
     *
     * @param context The invocation context
     * @return The ContainerRequestContext if found, null otherwise
     */
    private ContainerRequestContext extractRequestContext(final InvocationContext context)
    {
        final Object[] parameters = context.getParameters();
        if (parameters == null || parameters.length == 0)
        {
            return null;
        }

        // Filter methods receive ContainerRequestContext (or ResteasyReactiveContainerRequestContext) as first parameter
        final Object firstParam = parameters[0];
        if (firstParam instanceof ContainerRequestContext)
        {
            return (ContainerRequestContext) firstParam;
        }

        return null;
    }
}
