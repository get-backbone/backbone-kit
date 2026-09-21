package io.backbonehq.kit.security.api.domain;

import io.backbonehq.kit.security.api.dto.AuthResponse;

/**
 * Low-level domain provider interface for authenticating services via AWS Cognito User Pools using service account
 * credentials.
 * This provider performs stateless authentication operations (no caching).
 *
 * <p>This provider is used by:
 * - Auth-service endpoints (`/auth/service/login`, `/auth/service/refresh-token`) to authenticate services
 * - ServiceTokenProvider (which wraps this provider and adds caching/auto-refresh)
 *
 * <p>Service accounts are created in Cognito with the format: service-{service-name}
 * (e.g., service-document-service). The service JWT contains a custom:service_id claim
 * that identifies which service the token belongs to.
 *
 * <p>Implementations:
 * - CognitoServiceAuthenticationProvider (in auth-service, calls Cognito directly)
 * - RemoteServiceAuthenticationProvider (in other services, calls auth-service via HTTP)
 *
 * <p>For services that need cached, auto-refreshed tokens, use {@link ServiceTokenProvider} instead.
 */
public interface ServiceAuthenticationProvider
{
    /**
     * Authenticate a service with Cognito using service account credentials.
     *
     * @param serviceName     the name of the service (e.g., "document-service")
     * @param serviceUsername the service account username (e.g., "service-document-service")
     * @param servicePassword the service account password
     * @return authentication result containing service JWT tokens
     */
    AuthResponse authenticateService(final String serviceName, final String serviceUsername, final String servicePassword);

    /**
     * Refresh a service JWT token using a refresh token.
     *
     * @param serviceUsername the service account username (e.g., "service-document-service")
     * @param refreshToken    the refresh token from a previous authentication
     * @return authentication result with new service JWT tokens
     */
    AuthResponse refreshServiceToken(final String serviceUsername, final String refreshToken);
}
