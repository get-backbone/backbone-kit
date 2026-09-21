package io.backbonehq.kit.throttle.impl.infrastructure;

import io.backbonehq.kit.throttle.api.infrastructure.RateLimiterProperties;
import io.backbonehq.kit.throttle.impl.reference.ReferenceRateLimitingFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

/**
 * Conditional producer for {@link RateLimiterProperties}.
 *
 * <p>This producer only creates the {@link RateLimiterProperties} bean if all required
 * rate-limit configuration properties are present. This allows services without rate limiting
 * configured to start successfully, while services with rate limiting configured will have
 * the bean available.</p>
 *
 * <p>If properties are missing, the bean is not produced, which means:
 * - {@link Bucket4jRateLimiter} will not be created (since it requires this bean)
 * - {@link ReferenceRateLimitingFilter} will skip rate limiting (since it checks if the bean is resolvable)</p>
 */
@ApplicationScoped
public class RateLimiterPropertiesProducer
{
    private static final Logger LOGGER = Logger.getLogger(RateLimiterPropertiesProducer.class);

    private static final String AUTHENTICATED_CAPACITY = "rate-limit.authenticated-capacity-per-minute";
    private static final String UNAUTHENTICATED_CAPACITY = "rate-limit.unauthenticated-capacity-per-minute";
    private static final String AUTHENTICATED_REFILL = "rate-limit.authenticated-refill-per-second";
    private static final String UNAUTHENTICATED_REFILL = "rate-limit.unauthenticated-refill-per-second";

    @Produces
    @ApplicationScoped
    public RateLimiterProperties produceRateLimiterProperties()
    {
        final Config config = ConfigProvider.getConfig();
        if (hasAllRequiredProperties(config))
        {
            // All properties are present - create the implementation
            final long authenticatedCapacity = config.getValue(AUTHENTICATED_CAPACITY, Long.class);
            final long unauthenticatedCapacity = config.getValue(UNAUTHENTICATED_CAPACITY, Long.class);
            final long authenticatedRefill = config.getValue(AUTHENTICATED_REFILL, Long.class);
            final long unauthenticatedRefill = config.getValue(UNAUTHENTICATED_REFILL, Long.class);

            LOGGER.debugf("Rate limiting enabled: authenticated=%d/min (refill %d/s), unauthenticated=%d/min (refill %d/s)",
                authenticatedCapacity, authenticatedRefill, unauthenticatedCapacity, unauthenticatedRefill);

            return new RateLimiterPropertiesImpl(
                                                 authenticatedCapacity,
                                                 unauthenticatedCapacity,
                                                 authenticatedRefill,
                                                 unauthenticatedRefill
            );
        }

        LOGGER.info("Rate limiting properties not configured - rate limiting will be disabled");

        return null; // Don't produce the bean if properties are missing
    }

    private boolean hasAllRequiredProperties(final Config config)
    {
        return config.getOptionalValue(AUTHENTICATED_CAPACITY, Long.class).isPresent() && config.getOptionalValue(
            UNAUTHENTICATED_CAPACITY, Long.class).isPresent() && config.getOptionalValue(AUTHENTICATED_REFILL,
                Long.class).isPresent() && config.getOptionalValue(UNAUTHENTICATED_REFILL, Long.class).isPresent();
    }

    /**
     * Simple implementation of {@link RateLimiterProperties}.
     */
    private record RateLimiterPropertiesImpl(long authenticatedCapacityPerMinute, long unauthenticatedCapacityPerMinute,
                                             long authenticatedRefillPerSecond,
                                             long unauthenticatedRefillPerSecond) implements RateLimiterProperties
    {

    }
}
