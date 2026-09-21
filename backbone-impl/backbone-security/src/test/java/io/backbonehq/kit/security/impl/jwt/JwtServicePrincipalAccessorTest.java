package io.backbonehq.kit.security.impl.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServicePrincipalAccessorTest
{
    private final JwtPrincipalResolver principalResolver = mock(JwtPrincipalResolver.class);

    @Test
    @DisplayName("Extracts service ID from service principal")
    void extractsServiceIdFromServicePrincipal()
    {
        final JwtPrincipal.Service servicePrincipal = new JwtPrincipal.Service("service-123");
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.of(servicePrincipal));

        final JwtServicePrincipalAccessor accessor = new JwtServicePrincipalAccessor(principalResolver);
        final String result = accessor.extractServiceId("token");

        assertEquals("service-123", result);
    }

    @Test
    @DisplayName("Returns null when resolved principal is not a service principal")
    void returnsNullWhenResolvedPrincipalIsNotAServicePrincipal()
    {
        final JwtPrincipal.User userPrincipal = new JwtPrincipal.User("user@example.com");
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.of(userPrincipal));

        final JwtServicePrincipalAccessor accessor = new JwtServicePrincipalAccessor(principalResolver);
        final String result = accessor.extractServiceId("token");

        assertNull(result);
    }

    @Test
    @DisplayName("Returns null when resolver returns empty")
    void returnsNullWhenResolverReturnsEmpty()
    {
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.empty());

        final JwtServicePrincipalAccessor accessor = new JwtServicePrincipalAccessor(principalResolver);
        final String result = accessor.extractServiceId("token");

        assertNull(result);
    }

    @Test
    @DisplayName("Has service ID claim returns true when service principal is resolved")
    void hasServiceIdClaimReturnsTrueWhenServicePrincipalIsResolved()
    {
        final JwtPrincipal.Service servicePrincipal = new JwtPrincipal.Service("service-123");
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.of(servicePrincipal));

        final JwtServicePrincipalAccessor accessor = new JwtServicePrincipalAccessor(principalResolver);
        final boolean result = accessor.hasServiceIdClaim("token");

        assertTrue(result);
    }

    @Test
    @DisplayName("Has service ID claim returns false when user principal is resolved")
    void hasServiceIdClaimReturnsFalseWhenUserPrincipalIsResolved()
    {
        final JwtPrincipal.User userPrincipal = new JwtPrincipal.User("user@example.com");
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.of(userPrincipal));

        final JwtServicePrincipalAccessor accessor = new JwtServicePrincipalAccessor(principalResolver);
        final boolean result = accessor.hasServiceIdClaim("token");

        assertFalse(result);
    }

    @Test
    @DisplayName("Has service ID claim returns false when resolver returns empty")
    void hasServiceIdClaimReturnsFalseWhenResolverReturnsEmpty()
    {
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.empty());

        final JwtServicePrincipalAccessor accessor = new JwtServicePrincipalAccessor(principalResolver);
        final boolean result = accessor.hasServiceIdClaim("token");

        assertFalse(result);
    }
}
