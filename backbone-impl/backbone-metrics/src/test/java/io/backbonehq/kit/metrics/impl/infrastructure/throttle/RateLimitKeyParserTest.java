package io.backbonehq.kit.metrics.impl.infrastructure.throttle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitKeyParserTest
{
    @Test
    @DisplayName("extractKeyType returns 'user' for user prefix")
    void extractKeyType_ReturnsUser_ForUserPrefix()
    {
        assertEquals("user", RateLimitKeyParser.extractKeyType("user:john"));
        assertEquals("user", RateLimitKeyParser.extractKeyType("user:user@example.com"));
        assertEquals("user", RateLimitKeyParser.extractKeyType("user:test-user-123"));
    }

    @Test
    @DisplayName("extractKeyType returns 'service' for service prefix")
    void extractKeyType_ReturnsService_ForServicePrefix()
    {
        assertEquals("service", RateLimitKeyParser.extractKeyType("service:document-service"));
        assertEquals("service", RateLimitKeyParser.extractKeyType("service:actor-service"));
        assertEquals("service", RateLimitKeyParser.extractKeyType("service:api"));
    }

    @Test
    @DisplayName("extractKeyType returns 'ip' for IP prefix")
    void extractKeyType_ReturnsIp_ForIpPrefix()
    {
        assertEquals("ip", RateLimitKeyParser.extractKeyType("ip:192.168.1.1"));
        assertEquals("ip", RateLimitKeyParser.extractKeyType("ip:10.0.0.1"));
        assertEquals("ip", RateLimitKeyParser.extractKeyType("ip:unknown"));
    }

    @Test
    @DisplayName("extractKeyType returns 'auth' for auth prefix")
    void extractKeyType_ReturnsAuth_ForAuthPrefix()
    {
        assertEquals("auth", RateLimitKeyParser.extractKeyType("auth:unidentified"));
        assertEquals("auth", RateLimitKeyParser.extractKeyType("auth:invalid"));
    }

    @Test
    @DisplayName("extractKeyType returns 'unknown' for null key")
    void extractKeyType_ReturnsUnknown_ForNullKey()
    {
        assertEquals("unknown", RateLimitKeyParser.extractKeyType(null));
    }

    @Test
    @DisplayName("extractKeyType returns 'unknown' for empty key")
    void extractKeyType_ReturnsUnknown_ForEmptyKey()
    {
        assertEquals("unknown", RateLimitKeyParser.extractKeyType(""));
    }

    @Test
    @DisplayName("extractKeyType returns 'unknown' for blank key")
    void extractKeyType_ReturnsUnknown_ForBlankKey()
    {
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("   "));
    }

    @Test
    @DisplayName("extractKeyType returns 'unknown' for key without known prefix")
    void extractKeyType_ReturnsUnknown_ForKeyWithoutKnownPrefix()
    {
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("unknown:value"));
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("invalid"));
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("prefix:value"));
    }

    @Test
    @DisplayName("extractKeyType returns 'unknown' for key that starts with prefix substring")
    void extractKeyType_ReturnsUnknown_ForKeyStartingWithPrefixSubstring()
    {
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("users:john"));
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("services:api"));
        assertEquals("unknown", RateLimitKeyParser.extractKeyType("ips:192.168.1.1"));
    }

    @Test
    @DisplayName("extractIdentifier returns username for user prefix")
    void extractIdentifier_ReturnsUsername_ForUserPrefix()
    {
        assertEquals("john", RateLimitKeyParser.extractIdentifier("user:john"));
        assertEquals("user@example.com", RateLimitKeyParser.extractIdentifier("user:user@example.com"));
        assertEquals("test-user-123", RateLimitKeyParser.extractIdentifier("user:test-user-123"));
    }

    @Test
    @DisplayName("extractIdentifier returns serviceId for service prefix")
    void extractIdentifier_ReturnsServiceId_ForServicePrefix()
    {
        assertEquals("document-service", RateLimitKeyParser.extractIdentifier("service:document-service"));
        assertEquals("actor-service", RateLimitKeyParser.extractIdentifier("service:actor-service"));
        assertEquals("api", RateLimitKeyParser.extractIdentifier("service:api"));
    }

    @Test
    @DisplayName("extractIdentifier returns IP address for IP prefix")
    void extractIdentifier_ReturnsIpAddress_ForIpPrefix()
    {
        assertEquals("192.168.1.1", RateLimitKeyParser.extractIdentifier("ip:192.168.1.1"));
        assertEquals("10.0.0.1", RateLimitKeyParser.extractIdentifier("ip:10.0.0.1"));
        assertEquals("unknown", RateLimitKeyParser.extractIdentifier("ip:unknown"));
    }

    @Test
    @DisplayName("extractIdentifier returns identifier for auth prefix")
    void extractIdentifier_ReturnsIdentifier_ForAuthPrefix()
    {
        assertEquals("unidentified", RateLimitKeyParser.extractIdentifier("auth:unidentified"));
        assertEquals("invalid", RateLimitKeyParser.extractIdentifier("auth:invalid"));
    }

    @Test
    @DisplayName("extractIdentifier returns 'unknown' for null key")
    void extractIdentifier_ReturnsUnknown_ForNullKey()
    {
        assertEquals("unknown", RateLimitKeyParser.extractIdentifier(null));
    }

    @Test
    @DisplayName("extractIdentifier returns 'unknown' for empty key")
    void extractIdentifier_ReturnsUnknown_ForEmptyKey()
    {
        assertEquals("unknown", RateLimitKeyParser.extractIdentifier(""));
    }

    @Test
    @DisplayName("extractIdentifier returns 'unknown' for blank key")
    void extractIdentifier_ReturnsUnknown_ForBlankKey()
    {
        assertEquals("unknown", RateLimitKeyParser.extractIdentifier("   "));
    }

    @Test
    @DisplayName("extractIdentifier returns full key for key without known prefix")
    void extractIdentifier_ReturnsFullKey_ForKeyWithoutKnownPrefix()
    {
        assertEquals("unknown:value", RateLimitKeyParser.extractIdentifier("unknown:value"));
        assertEquals("invalid", RateLimitKeyParser.extractIdentifier("invalid"));
        assertEquals("prefix:value", RateLimitKeyParser.extractIdentifier("prefix:value"));
    }

    @Test
    @DisplayName("extractIdentifier handles keys with only prefix")
    void extractIdentifier_HandlesKeysWithOnlyPrefix()
    {
        assertEquals("", RateLimitKeyParser.extractIdentifier("user:"));
        assertEquals("", RateLimitKeyParser.extractIdentifier("service:"));
        assertEquals("", RateLimitKeyParser.extractIdentifier("ip:"));
        assertEquals("", RateLimitKeyParser.extractIdentifier("auth:"));
    }

    @Test
    @DisplayName("extractIdentifier handles keys with special characters in identifier")
    void extractIdentifier_HandlesKeysWithSpecialCharacters()
    {
        assertEquals("user@example.com", RateLimitKeyParser.extractIdentifier("user:user@example.com"));
        assertEquals("192.168.1.1:8080", RateLimitKeyParser.extractIdentifier("ip:192.168.1.1:8080"));
        assertEquals("service-name-v1", RateLimitKeyParser.extractIdentifier("service:service-name-v1"));
    }

    @Test
    @DisplayName("extractIdentifier handles keys with colons in identifier")
    void extractIdentifier_HandlesKeysWithColonsInIdentifier()
    {
        assertEquals("user:name", RateLimitKeyParser.extractIdentifier("user:user:name"));
        assertEquals("ip:address", RateLimitKeyParser.extractIdentifier("ip:ip:address"));
    }
}
