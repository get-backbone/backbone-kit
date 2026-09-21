package io.backbonehq.kit.security.impl.jwt;

import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Accessor for user principal claims from JWT tokens.
 * <p>
 * This class provides convenient methods for extracting username claims from JWT tokens.
 * It uses {@link JwtPrincipalResolver} to resolve principals and filters for user principals.
 */
@ApplicationScoped
public final class JwtUserPrincipalAccessor
{
    private final JwtPrincipalResolver principalResolver;

    @Inject
    public JwtUserPrincipalAccessor(final JwtPrincipalResolver principalResolver)
    {
        this.principalResolver = principalResolver;
    }

    /**
     * Extracts username from a JWT token (ID token or refresh token).
     *
     * @param jwtToken the JWT token
     * @return username if found, null otherwise
     */
    public String extractUsername(final String jwtToken)
    {
        return principalResolver.resolveFromToken(jwtToken)
            .filter(JwtPrincipal.User.class::isInstance)
            .map(JwtPrincipal.User.class::cast)
            .map(JwtPrincipal.User::username)
            .orElse(null);
    }
}
