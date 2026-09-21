package io.backbonehq.kit.throttle.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.backbonehq.kit.throttle.api.infrastructure.RateLimiterProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimiterPropertiesTest
{
    private static final long AUTH_CAPACITY = 100L;
    private static final long UNAUTH_CAPACITY = 50L;
    private static final long AUTH_REFILL = 10L;
    private static final long UNAUTH_REFILL = 5L;

    private final RateLimiterProperties properties = new TestRateLimiterProperties();

    @Test
    @DisplayName("Resolves authenticated capacity for user key")
    void resolvesAuthenticatedCapacityForUserKey()
    {
        assertEquals(AUTH_CAPACITY, properties.resolveCapacityForKey("user:test@example.com"));
    }

    @Test
    @DisplayName("Resolves authenticated capacity for service key")
    void resolvesAuthenticatedCapacityForServiceKey()
    {
        assertEquals(AUTH_CAPACITY, properties.resolveCapacityForKey("service:api-service"));
    }

    @Test
    @DisplayName("Resolves authenticated capacity for auth key")
    void resolvesAuthenticatedCapacityForAuthKey()
    {
        assertEquals(AUTH_CAPACITY, properties.resolveCapacityForKey("auth:unidentified"));
    }

    @Test
    @DisplayName("Resolves unauthenticated capacity for IP key")
    void resolvesUnauthenticatedCapacityForIpKey()
    {
        assertEquals(UNAUTH_CAPACITY, properties.resolveCapacityForKey("ip:192.168.1.1"));
    }

    @Test
    @DisplayName("Defaults to authenticated capacity for unknown key prefix")
    void defaultsToAuthenticatedCapacityForUnknownKeyPrefix()
    {
        assertEquals(AUTH_CAPACITY, properties.resolveCapacityForKey("unknown:key"));
    }

    @Test
    @DisplayName("Resolves authenticated refill for user key")
    void resolvesAuthenticatedRefillForUserKey()
    {
        assertEquals(AUTH_REFILL, properties.resolveRefillPerSecondForKey("user:test@example.com"));
    }

    @Test
    @DisplayName("Resolves authenticated refill for service key")
    void resolvesAuthenticatedRefillForServiceKey()
    {
        assertEquals(AUTH_REFILL, properties.resolveRefillPerSecondForKey("service:api-service"));
    }

    @Test
    @DisplayName("Resolves authenticated refill for auth key")
    void resolvesAuthenticatedRefillForAuthKey()
    {
        assertEquals(AUTH_REFILL, properties.resolveRefillPerSecondForKey("auth:unidentified"));
    }

    @Test
    @DisplayName("Resolves unauthenticated refill for IP key")
    void resolvesUnauthenticatedRefillForIpKey()
    {
        assertEquals(UNAUTH_REFILL, properties.resolveRefillPerSecondForKey("ip:192.168.1.1"));
    }

    @Test
    @DisplayName("Defaults to authenticated refill for unknown key prefix")
    void defaultsToAuthenticatedRefillForUnknownKeyPrefix()
    {
        assertEquals(AUTH_REFILL, properties.resolveRefillPerSecondForKey("unknown:key"));
    }

    private static class TestRateLimiterProperties implements RateLimiterProperties
    {
        @Override
        public long authenticatedCapacityPerMinute()
        {
            return AUTH_CAPACITY;
        }

        @Override
        public long unauthenticatedCapacityPerMinute()
        {
            return UNAUTH_CAPACITY;
        }

        @Override
        public long authenticatedRefillPerSecond()
        {
            return AUTH_REFILL;
        }

        @Override
        public long unauthenticatedRefillPerSecond()
        {
            return UNAUTH_REFILL;
        }
    }
}
