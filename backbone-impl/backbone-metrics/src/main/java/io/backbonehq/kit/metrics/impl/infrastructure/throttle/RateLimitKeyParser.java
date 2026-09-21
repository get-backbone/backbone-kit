package io.backbonehq.kit.metrics.impl.infrastructure.throttle;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for parsing rate limit keys to extract key type and identifier.
 * Used for metrics tagging and reporting.
 *
 * <p>Also provides constants for request context property names used to pass rate limit data
 * from filters to metrics handlers.
 */
public final class RateLimitKeyParser
{
    /**
     * Property name for storing rate limit key in request context.
     */
    public static final String RATE_LIMIT_KEY_PROPERTY = "rateLimitKey";

    /**
     * Property name for storing rate limit status allowed flag in request context.
     */
    public static final String RATE_LIMIT_STATUS_ALLOWED_PROPERTY = "rateLimitStatusAllowed";

    /**
     * Property name for storing rate limit capacity in request context.
     */
    public static final String RATE_LIMIT_STATUS_LIMIT_PROPERTY = "rateLimitStatusLimit";

    /**
     * Property name for storing rate limit remaining tokens in request context.
     */
    public static final String RATE_LIMIT_STATUS_REMAINING_PROPERTY = "rateLimitStatusRemaining";

    private static final String USER_PREFIX = "user:";
    private static final String SERVICE_PREFIX = "service:";
    private static final String IP_PREFIX = "ip:";
    private static final String AUTH_PREFIX = "auth:";
    private static final String UNKNOWN = "unknown";

    private static final Map<String, String> PREFIX_TO_TYPE = Map.of(
        USER_PREFIX, "user",
        SERVICE_PREFIX, "service",
        IP_PREFIX, "ip",
        AUTH_PREFIX, "auth"
    );

    private RateLimitKeyParser()
    {
        // Utility class
    }

    /**
     * Extracts the key type from a rate limit key.
     *
     * @param key The rate limit key (e.g., "user:john", "service:api", "ip:192.168.1.1", "auth:unidentified")
     * @return The key type ("user", "service", "ip", or "auth")
     */
    public static String extractKeyType(final String key)
    {
        if (StringUtils.isBlank(key))
        {
            return UNKNOWN;
        }

        for (Map.Entry<String, String> entry : PREFIX_TO_TYPE.entrySet())
        {
            if (key.startsWith(entry.getKey()))
            {
                return entry.getValue();
            }
        }

        return UNKNOWN;
    }

    /**
     * Extracts the identifier from a rate limit key.
     *
     * @param key The rate limit key (e.g., "user:john", "service:api", "ip:192.168.1.1", "auth:unidentified")
     * @return The identifier (username, serviceId, IP address, or "unidentified")
     */
    public static String extractIdentifier(final String key)
    {
        if (StringUtils.isBlank(key))
        {
            return UNKNOWN;
        }

        for (final String prefix : new String[]{USER_PREFIX, SERVICE_PREFIX, IP_PREFIX, AUTH_PREFIX})
        {
            if (key.startsWith(prefix))
            {
                return key.substring(prefix.length());
            }
        }

        return key;
    }
}
