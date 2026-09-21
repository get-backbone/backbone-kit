package io.backbonehq.kit.security.api.domain;

import java.util.Optional;

/**
 * High-level domain provider interface for obtaining service JWT tokens with caching and auto-refresh.
 * Services use this to get their own service JWT token for service-to-service authentication.
 *
 * <p>This provider wraps {@link ServiceAuthenticationProvider} and adds:
 * - Token caching (avoids repeated authentication calls)
 * - Automatic refresh before expiration (with 60 second buffer)
 * - Fallback to re-authentication if refresh fails
 *
 * <p>This provider is used by services to:
 * - Get their service JWT token for making service-to-service calls (via ServiceTokenClientRequestFilter)
 * - Automatically refresh tokens when they expire
 * - Cache tokens to avoid repeated authentication calls
 *
 * <p>For low-level, stateless authentication operations, use {@link ServiceAuthenticationProvider} instead.
 */
public interface ServiceTokenProvider
{
    /**
     * Get the service JWT token for this service.
     * Returns cached token if valid, otherwise authenticates and caches a new token.
     *
     * @return the service JWT token, or empty if service authentication is not configured
     */
    Optional<String> getServiceToken();

    /**
     * Refresh the service JWT token.
     * Forces a new authentication or refresh, bypassing the cache.
     *
     * @return the new service JWT token, or empty if service authentication is not configured
     */
    Optional<String> refreshServiceToken();

    /**
     * Get the service ID for this service.
     * This is the service identifier that will be in the custom:service_id claim of the JWT.
     *
     * @return the service ID (e.g., "document-service"), or empty if not configured
     */
    String getServiceId();
}
