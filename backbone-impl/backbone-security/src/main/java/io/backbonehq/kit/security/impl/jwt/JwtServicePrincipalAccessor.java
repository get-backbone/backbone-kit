package io.backbonehq.kit.security.impl.jwt;

import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Accessor for service principal claims from JWT tokens.
 * <p>
 * This class provides convenient methods for extracting service ID claims from JWT tokens.
 * It uses {@link JwtPrincipalResolver} to resolve principals and filters for service principals.
 * </p>
 */
@ApplicationScoped
public final class JwtServicePrincipalAccessor
{
    private final JwtPrincipalResolver principalResolver;

    @Inject
    public JwtServicePrincipalAccessor(final JwtPrincipalResolver principalResolver)
    {
        this.principalResolver = principalResolver;
    }

    /**
     * Extracts service ID from a JWT token (Cognito-specific: custom:service_id claim).
     *
     * @param jwtToken the JWT token
     * @return service ID if found, null otherwise
     */
    public String extractServiceId(final String jwtToken)
    {
        return principalResolver.resolveFromToken(jwtToken)
            .filter(JwtPrincipal.Service.class::isInstance)
            .map(JwtPrincipal.Service.class::cast)
            .map(JwtPrincipal.Service::serviceId)
            .orElse(null);
    }

    /**
     * Checks if a JWT token has a service ID claim.
     *
     * @param jwtToken the JWT token
     * @return true if service ID claim exists, false otherwise
     */
    public boolean hasServiceIdClaim(final String jwtToken)
    {
        return extractServiceId(jwtToken) != null;
    }
}
