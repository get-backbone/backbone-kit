package io.backbonehq.kit.throttle.impl.key.resolver.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import io.backbonehq.kit.security.impl.jwt.JwtPrincipalResolver;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtRateLimitPrincipalKeyResolverTest
{
    private final JwtPrincipalResolver principalResolver = mock(JwtPrincipalResolver.class);

    @Test
    @DisplayName("Returns anonymous key when authorization header is null")
    void returnsAnonymousKeyWhenHeaderIsNull()
    {
        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve(null);

        assertEquals("auth:unidentified", result);
    }

    @Test
    @DisplayName("Returns anonymous key when authorization header does not start with Bearer")
    void returnsAnonymousKeyWhenHeaderDoesNotStartWithBearer()
    {
        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");

        assertEquals("auth:unidentified", result);
    }

    @Test
    @DisplayName("Returns anonymous key when authorization header is empty")
    void returnsAnonymousKeyWhenHeaderIsEmpty()
    {
        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve("");

        assertEquals("auth:unidentified", result);
    }

    @Test
    @DisplayName("Returns anonymous key when resolver returns empty")
    void returnsAnonymousKeyWhenResolverReturnsEmpty()
    {
        when(principalResolver.resolveFromToken("token123")).thenReturn(Optional.empty());

        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve("Bearer token123");

        assertEquals("auth:unidentified", result);
    }

    @Test
    @DisplayName("Returns service key when resolver returns service principal")
    void returnsServiceKeyWhenResolverReturnsServicePrincipal()
    {
        final JwtPrincipal.Service servicePrincipal = new JwtPrincipal.Service("service-123");
        when(principalResolver.resolveFromToken("token123")).thenReturn(Optional.of(servicePrincipal));

        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve("Bearer token123");

        assertEquals("service:service-123", result);
    }

    @Test
    @DisplayName("Returns user key when resolver returns user principal")
    void returnsUserKeyWhenResolverReturnsUserPrincipal()
    {
        final JwtPrincipal.User userPrincipal = new JwtPrincipal.User("user@example.com");
        when(principalResolver.resolveFromToken("token123")).thenReturn(Optional.of(userPrincipal));

        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve("Bearer token123");

        assertEquals("user:user@example.com", result);
    }

    @Test
    @DisplayName("Extracts token correctly from Bearer header")
    void extractsTokenCorrectlyFromBearerHeader()
    {
        final JwtPrincipal.Service servicePrincipal = new JwtPrincipal.Service("service-123");
        when(principalResolver.resolveFromToken("my.jwt.token")).thenReturn(Optional.of(servicePrincipal));

        final JwtRateLimitPrincipalKeyResolver rateLimitPrincipalKeyResolver = new JwtRateLimitPrincipalKeyResolver(principalResolver);
        final String result = rateLimitPrincipalKeyResolver.resolve("Bearer my.jwt.token");

        assertEquals("service:service-123", result);
    }
}
