package io.backbonehq.kit.throttle.api.infrastructure;

import org.apache.commons.lang3.Strings;

/**
 * Configuration interface for rate limiter defaults.
 *
 * <p>This interface is produced conditionally by {@link RateLimiterPropertiesProducer}
 * only when all required rate-limit configuration properties are present. This allows
 * services without rate limiting configured to start successfully.</p>
 *
 * <p>This initial version provides simple defaults based on key prefixes. It can be
 * extended later with per-endpoint configuration without changing the limiter API.</p>
 */
public interface RateLimiterProperties
{
    long authenticatedCapacityPerMinute();

    long unauthenticatedCapacityPerMinute();

    long authenticatedRefillPerSecond();

    long unauthenticatedRefillPerSecond();

    default long resolveCapacityForKey(final String key)
    {
        if (Strings.CS.startsWithAny(key, "user:", "service:", "auth:"))
        {
            return authenticatedCapacityPerMinute();
        }

        if (key.startsWith("ip:"))
        {
            return unauthenticatedCapacityPerMinute();
        }

        return authenticatedCapacityPerMinute();
    }

    default long resolveRefillPerSecondForKey(final String key)
    {
        if (Strings.CS.startsWithAny(key, "user:", "service:", "auth:"))
        {
            return authenticatedRefillPerSecond();
        }

        if (key.startsWith("ip:"))
        {
            return unauthenticatedRefillPerSecond();
        }

        return authenticatedRefillPerSecond();
    }
}
