package io.backbonehq.kit.throttle.impl.key.resolver.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpHeaderRateLimitIpKeyResolverTest
{
    private final HttpHeaderRateLimitIpKeyResolver resolver = new HttpHeaderRateLimitIpKeyResolver();

    @Test
    @DisplayName("Returns IP from X-Forwarded-For header")
    void returnsIpFromXForwardedForHeader()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:192.168.1.1", result);
    }

    @Test
    @DisplayName("Returns first IP from X-Forwarded-For header with multiple IPs")
    void returnsFirstIpFromXForwardedForHeaderWithMultipleIps()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:192.168.1.1", result);
    }

    @Test
    @DisplayName("Trims whitespace from X-Forwarded-For IP")
    void trimsWhitespaceFromXForwardedForIp()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("  192.168.1.1  ");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:192.168.1.1", result);
    }

    @Test
    @DisplayName("Returns IP from X-Real-IP header when X-Forwarded-For is missing")
    void returnsIpFromXRealIpHeaderWhenXForwardedForMissing()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn("10.0.0.1");
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:10.0.0.1", result);
    }

    @Test
    @DisplayName("Returns IP from X-Real-IP header when X-Forwarded-For is blank")
    void returnsIpFromXRealIpHeaderWhenXForwardedForBlank()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("   ");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn("10.0.0.1");
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:10.0.0.1", result);
    }

    @Test
    @DisplayName("Returns IP from Remote-Addr header when X-Forwarded-For and X-Real-IP are missing")
    void returnsIpFromRemoteAddrHeaderWhenOtherHeadersMissing()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn("172.16.0.1");

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:172.16.0.1", result);
    }

    @Test
    @DisplayName("Returns unknown when all headers are missing")
    void returnsUnknownWhenAllHeadersMissing()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:unknown", result);
    }

    @Test
    @DisplayName("Returns unknown when all headers are blank")
    void returnsUnknownWhenAllHeadersBlank()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("   ");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn("   ");
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn("   ");

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:unknown", result);
    }

    @Test
    @DisplayName("Prefers X-Forwarded-For over X-Real-IP when both are present")
    void prefersXForwardedForOverXRealIpWhenBothPresent()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn("10.0.0.1");
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn("172.16.0.1");

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:192.168.1.1", result);
    }

    @Test
    @DisplayName("Prefers X-Real-IP over Remote-Addr when X-Forwarded-For is missing")
    void prefersXRealIpOverRemoteAddrWhenXForwardedForMissing()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn(null);
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn("10.0.0.1");
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn("172.16.0.1");

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:10.0.0.1", result);
    }

    @Test
    @DisplayName("Handles X-Forwarded-For with comma but no space")
    void handlesXForwardedForWithCommaButNoSpace()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("192.168.1.1,10.0.0.1");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:192.168.1.1", result);
    }

    @Test
    @DisplayName("Handles IPv6 address in X-Forwarded-For")
    void handlesIpv6AddressInXForwardedFor()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn(null);
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:2001:0db8:85a3:0000:0000:8a2e:0370:7334", result);
    }

    @Test
    @DisplayName("Falls back to IP when X-Forwarded-For has only commas")
    void fallsBackToIpWhenXForwardedForHasOnlyCommas()
    {
        final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString("X-Forwarded-For")).thenReturn(", ,");
        when(requestContext.getHeaderString("X-Real-IP")).thenReturn("10.0.0.1");
        when(requestContext.getHeaderString("Remote-Addr")).thenReturn(null);

        final String result = resolver.resolve(requestContext);

        assertEquals("ip:10.0.0.1", result);
    }
}
