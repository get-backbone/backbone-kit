package io.backbonehq.kit.security.impl.jwt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenExtractorTest
{
    @Test
    @DisplayName("extractFromAuthorizationHeader extracts token from Bearer header")
    void extractFromAuthorizationHeader_ExtractsToken_FromBearerHeader()
    {
        final String header = "Bearer abc123token";
        final String result = new JwtTokenExtractor().extractFromAuthorizationHeader(header);

        assertEquals("abc123token", result);
    }

    @Test
    @DisplayName("extractFromAuthorizationHeader trims whitespace")
    void extractFromAuthorizationHeader_TrimsWhitespace()
    {
        final String header = "Bearer   token with spaces  ";
        final String result = new JwtTokenExtractor().extractFromAuthorizationHeader(header);

        assertEquals("token with spaces", result);
    }

    @Test
    @DisplayName("extractFromAuthorizationHeader returns empty string when header is null")
    void extractFromAuthorizationHeader_ReturnsEmptyString_WhenHeaderIsNull()
    {
        final String result = new JwtTokenExtractor().extractFromAuthorizationHeader(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("extractFromAuthorizationHeader returns empty string when header doesn't start with Bearer")
    void extractFromAuthorizationHeader_ReturnsEmptyString_WhenHeaderDoesNotStartWithBearer()
    {
        final String header = "Basic abc123";
        final String result = new JwtTokenExtractor().extractFromAuthorizationHeader(header);

        assertEquals("", result);
    }

    @Test
    @DisplayName("isBearerToken returns true for Bearer token")
    void isBearerToken_ReturnsTrue_ForBearerToken()
    {
        final String header = "Bearer token123";
        final boolean result = new JwtTokenExtractor().isBearerToken(header);

        assertTrue(result);
    }

    @Test
    @DisplayName("isBearerToken returns false for null")
    void isBearerToken_ReturnsFalse_ForNull()
    {
        //noinspection ConstantValue
        assertFalse(new JwtTokenExtractor().isBearerToken(null));
    }

    @Test
    @DisplayName("isBearerToken returns false for non-Bearer header")
    void isBearerToken_ReturnsFalse_ForNonBearerHeader()
    {
        final String header = "Basic abc123";
        final boolean result = new JwtTokenExtractor().isBearerToken(header);

        assertFalse(result);
    }
}
