package io.backbonehq.kit.security.impl.jwt;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Extracts JWT tokens from HTTP headers.
 */
@ApplicationScoped
public final class JwtTokenExtractor
{
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Extracts the JWT token from an Authorization header.
     *
     * @param authorizationHeader The Authorization header value
     * @return The JWT token, or empty string if header is null or doesn't start with "Bearer "
     */
    public String extractFromAuthorizationHeader(final String authorizationHeader)
    {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX))
        {
            return "";
        }

        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }

    /**
     * Checks if a header value is a Bearer token.
     *
     * @param headerValue The header value to check
     * @return true if the header starts with "Bearer ", false otherwise
     */
    public boolean isBearerToken(final String headerValue)
    {
        return headerValue != null && headerValue.startsWith(BEARER_PREFIX);
    }
}
