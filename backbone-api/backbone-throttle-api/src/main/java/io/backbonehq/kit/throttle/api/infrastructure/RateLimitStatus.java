package io.backbonehq.kit.throttle.api.infrastructure;

/**
 * Immutable value object representing the outcome of a rate limit check.
 */
public record RateLimitStatus(boolean allowed, long limit, long remaining, long retryAfterSeconds)
{
}
