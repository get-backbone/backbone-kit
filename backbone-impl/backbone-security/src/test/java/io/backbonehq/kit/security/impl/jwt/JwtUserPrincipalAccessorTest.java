package io.backbonehq.kit.security.impl.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUserPrincipalAccessorTest
{
    private final JwtPrincipalResolver principalResolver = mock(JwtPrincipalResolver.class);

    @Test
    @DisplayName("Extracts username from user principal")
    void extractsUsernameFromUserPrincipal()
    {
        final JwtPrincipal.User userPrincipal = new JwtPrincipal.User("user@example.com");
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.of(userPrincipal));

        final JwtUserPrincipalAccessor accessor = new JwtUserPrincipalAccessor(principalResolver);
        final String result = accessor.extractUsername("token");

        assertEquals("user@example.com", result);
    }

    @Test
    @DisplayName("Returns null when resolved principal is not a user principal")
    void returnsNullWhenResolvedPrincipalIsNotAUserPrincipal()
    {
        final JwtPrincipal.Service servicePrincipal = new JwtPrincipal.Service("service-123");
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.of(servicePrincipal));

        final JwtUserPrincipalAccessor accessor = new JwtUserPrincipalAccessor(principalResolver);
        final String result = accessor.extractUsername("token");

        assertNull(result);
    }

    @Test
    @DisplayName("Returns null when resolver returns empty")
    void returnsNullWhenResolverReturnsEmpty()
    {
        when(principalResolver.resolveFromToken("token")).thenReturn(Optional.empty());

        final JwtUserPrincipalAccessor accessor = new JwtUserPrincipalAccessor(principalResolver);
        final String result = accessor.extractUsername("token");

        assertNull(result);
    }
}
