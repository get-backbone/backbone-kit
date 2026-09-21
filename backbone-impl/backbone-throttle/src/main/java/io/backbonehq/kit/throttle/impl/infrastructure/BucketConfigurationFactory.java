package io.backbonehq.kit.throttle.impl.infrastructure;

import io.backbonehq.kit.throttle.api.infrastructure.RateLimiterProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import java.time.Duration;

/**
 * Builds Bucket4j bucket configurations from rate limiter properties.
 */
final class BucketConfigurationFactory
{
    private BucketConfigurationFactory()
    {
    }

    static BucketConfiguration createConfiguration(final RateLimiterProperties properties, final String key)
    {
        return BucketConfiguration.builder()
            .addLimit(createBandwidth(properties, key))
            .build();
    }

    static Bandwidth createBandwidth(final RateLimiterProperties properties, final String key)
    {
        final long capacity = properties.resolveCapacityForKey(key);
        final long refillPerSecond = properties.resolveRefillPerSecondForKey(key);

        // refill=0 means no practical refill (keeps RateLimitingIT deterministic under slow JVM/CI).
        if (refillPerSecond <= 0L)
        {
            return Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(1L, Duration.ofDays(365L))
                .build();
        }

        return Bandwidth.builder()
            .capacity(capacity)
            .refillIntervally(refillPerSecond, Duration.ofSeconds(1L))
            .build();
    }
}
