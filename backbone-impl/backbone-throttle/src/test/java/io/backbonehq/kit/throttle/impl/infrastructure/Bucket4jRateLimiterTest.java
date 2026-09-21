package io.backbonehq.kit.throttle.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.backbonehq.kit.throttle.api.infrastructure.RateLimitStatus;
import io.backbonehq.kit.throttle.api.infrastructure.RateLimiterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Bucket4jRateLimiterTest
{
    private Bucket4jRateLimiter rateLimiter;

    @BeforeEach
    void setUp()
    {
        final RateLimiterProperties properties = mock(RateLimiterProperties.class);
        when(properties.resolveCapacityForKey("test-key")).thenReturn(10L);
        when(properties.resolveRefillPerSecondForKey("test-key")).thenReturn(10L);
        when(properties.resolveCapacityForKey("ip-key")).thenReturn(5L);
        when(properties.resolveRefillPerSecondForKey("ip-key")).thenReturn(5L);

        rateLimiter = new Bucket4jRateLimiter(properties);
    }

    @Test
    @DisplayName("Allows request when bucket has capacity")
    void allowsRequestWhenBucketHasCapacity()
    {
        final RateLimitStatus status = rateLimiter.tryConsume("test-key");

        assertTrue(status.allowed());
        assertEquals(10L, status.limit());
        assertTrue(status.remaining() >= 0);
        assertEquals(0L, status.retryAfterSeconds());
    }

    @Test
    @DisplayName("Creates separate buckets for different keys")
    void createsSeparateBucketsForDifferentKeys()
    {
        // Consume all tokens for first key
        for (int i = 0; i < 10; i++)
        {
            rateLimiter.tryConsume("test-key");
        }

        // First key should be rate limited
        final RateLimitStatus status1 = rateLimiter.tryConsume("test-key");
        assertFalse(status1.allowed());

        // Second key should still have capacity
        final RateLimitStatus status2 = rateLimiter.tryConsume("ip-key");
        assertTrue(status2.allowed());
    }

    @Test
    @DisplayName("Returns correct limit from properties")
    void returnsCorrectLimitFromProperties()
    {
        final RateLimitStatus status = rateLimiter.tryConsume("test-key");

        assertEquals(10L, status.limit());
    }

    @Test
    @DisplayName("Returns correct limit for different key types")
    void returnsCorrectLimitForDifferentKeyTypes()
    {
        final RateLimitStatus status = rateLimiter.tryConsume("ip-key");

        assertEquals(5L, status.limit());
    }

    @Test
    @DisplayName("Returns retry after seconds when rate limited")
    void returnsRetryAfterSecondsWhenRateLimited()
    {
        // Consume all tokens
        for (int i = 0; i < 10; i++)
        {
            rateLimiter.tryConsume("test-key");
        }

        final RateLimitStatus status = rateLimiter.tryConsume("test-key");

        assertFalse(status.allowed());
        assertTrue(status.retryAfterSeconds() > 0);
    }

    @Test
    @DisplayName("Clears all buckets")
    void clearsAllBuckets()
    {
        // Consume tokens
        rateLimiter.tryConsume("test-key");
        rateLimiter.tryConsume("ip-key");

        // Clear buckets
        rateLimiter.clearBuckets();

        // Both keys should have fresh buckets with full capacity
        // After consuming 1 token, remaining should be capacity - 1
        final RateLimitStatus status1 = rateLimiter.tryConsume("test-key");
        final RateLimitStatus status2 = rateLimiter.tryConsume("ip-key");

        assertTrue(status1.allowed());
        assertTrue(status2.allowed());
        assertEquals(9L, status1.remaining()); // 10 - 1
        assertEquals(4L, status2.remaining()); // 5 - 1
    }
}
