package io.backbonehq.kit.throttle.impl.key.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.backbonehq.kit.throttle.api.key.resolver.http.IpHeaderRateLimitKeyResolver;
import io.backbonehq.kit.throttle.api.key.resolver.jwt.AuthHeaderRateLimitKeyResolver;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpHeaderRateLimitKeyStrategyTest
{
    @Test
    @DisplayName("Uses auth resolver when Authorization header is not blank")
    void usesAuthResolverWhenAuthorizationHeaderIsNotBlank()
    {
        final AuthHeaderRateLimitKeyResolver authResolver = mock(AuthHeaderRateLimitKeyResolver.class);
        final IpHeaderRateLimitKeyResolver ipResolver = mock(IpHeaderRateLimitKeyResolver.class);
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

        when(requestContext.getHeaderString("Authorization")).thenReturn("Bearer token123");
        when(authResolver.resolve("Bearer token123")).thenReturn("user:test@example.com");

        final HttpHeaderRateLimitKeyStrategy strategy = new HttpHeaderRateLimitKeyStrategy();
        strategy.authHeaderRateLimitKeyResolver = authResolver;
        strategy.ipHeaderRateLimitKeyResolver = ipResolver;

        final String result = strategy.resolve(requestContext);

        assertEquals("user:test@example.com", result);
        verify(authResolver).resolve(eq("Bearer token123"));
        verify(ipResolver, never()).resolve(any());
    }

    @Test
    @DisplayName("Uses IP resolver when Authorization header is null")
    void usesIpResolverWhenAuthorizationHeaderIsNull()
    {
        final AuthHeaderRateLimitKeyResolver authResolver = mock(AuthHeaderRateLimitKeyResolver.class);
        final IpHeaderRateLimitKeyResolver ipResolver = mock(IpHeaderRateLimitKeyResolver.class);
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

        when(requestContext.getHeaderString("Authorization")).thenReturn(null);
        when(ipResolver.resolve(requestContext)).thenReturn("ip:192.168.1.1");

        final HttpHeaderRateLimitKeyStrategy strategy = new HttpHeaderRateLimitKeyStrategy();
        strategy.authHeaderRateLimitKeyResolver = authResolver;
        strategy.ipHeaderRateLimitKeyResolver = ipResolver;

        final String result = strategy.resolve(requestContext);

        assertEquals("ip:192.168.1.1", result);
        verify(ipResolver).resolve(requestContext);
        verify(authResolver, never()).resolve(any());
    }

    @Test
    @DisplayName("Uses IP resolver when Authorization header is blank")
    void usesIpResolverWhenAuthorizationHeaderIsBlank()
    {
        final AuthHeaderRateLimitKeyResolver authResolver = mock(AuthHeaderRateLimitKeyResolver.class);
        final IpHeaderRateLimitKeyResolver ipResolver = mock(IpHeaderRateLimitKeyResolver.class);
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

        when(requestContext.getHeaderString("Authorization")).thenReturn("   ");
        when(ipResolver.resolve(requestContext)).thenReturn("ip:unknown");

        final HttpHeaderRateLimitKeyStrategy strategy = new HttpHeaderRateLimitKeyStrategy();
        strategy.authHeaderRateLimitKeyResolver = authResolver;
        strategy.ipHeaderRateLimitKeyResolver = ipResolver;

        final String result = strategy.resolve(requestContext);

        assertEquals("ip:unknown", result);
        verify(ipResolver).resolve(requestContext);
        verify(authResolver, never()).resolve(any());
    }
}
