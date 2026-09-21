package io.backbonehq.kit.security.impl.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtPayloadParserTest
{
    @Test
    @DisplayName("Parses valid JWT token with three parts")
    void parsesValidJwtTokenWithThreeParts()
    {
        final String payloadJson = "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022}";
        final String jwtToken = buildTestJwtToken(payloadJson);

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertTrue(result.isPresent());
        assertEquals("1234567890", result.get().get("sub").asText());
        assertEquals("John Doe", result.get().get("name").asText());
        assertEquals(1516239022, result.get().get("iat").asInt());
    }

    @Test
    @DisplayName("Parses JWT token with custom claims")
    void parsesJwtTokenWithCustomClaims()
    {
        final String payloadJson = "{\"cognito:username\":\"user@example.com\",\"custom:service_id\":\"service-123\"}";
        final String jwtToken = buildTestJwtToken(payloadJson);

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().get("cognito:username").asText());
        assertEquals("service-123", result.get().get("custom:service_id").asText());
    }

    @Test
    @DisplayName("Returns empty when token is null")
    void returnsEmptyWhenTokenIsNull()
    {
        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(null);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token is blank")
    void returnsEmptyWhenTokenIsBlank()
    {
        final Optional<JsonNode> result = JwtPayloadParser.parsePayload("   ");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token is empty string")
    void returnsEmptyWhenTokenIsEmptyString()
    {
        final Optional<JsonNode> result = JwtPayloadParser.parsePayload("");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token has less than three parts")
    void returnsEmptyWhenTokenHasLessThanThreeParts()
    {
        final Optional<JsonNode> result1 = JwtPayloadParser.parsePayload("header");
        final Optional<JsonNode> result2 = JwtPayloadParser.parsePayload("header.payload");

        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token has more than three parts")
    void returnsEmptyWhenTokenHasMoreThanThreeParts()
    {
        final Optional<JsonNode> result = JwtPayloadParser.parsePayload("header.payload.signature.extra");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handles JWT token without padding in base64")
    void handlesJwtTokenWithoutPaddingInBase64()
    {
        final String payloadJson = "{\"sub\":\"test\"}";
        final String jwtToken = buildTestJwtTokenWithoutPadding(payloadJson);

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertTrue(result.isPresent());
        assertEquals("test", result.get().get("sub").asText());
    }

    @Test
    @DisplayName("Returns empty when payload is invalid JSON")
    void returnsEmptyWhenPayloadIsInvalidJson()
    {
        final String invalidJson = "{invalid json}";
        final String header = base64UrlEncode("{\"alg\":\"none\"}");
        final String payload = base64UrlEncode(invalidJson);
        final String jwtToken = header + "." + payload + ".signature";

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when payload is not valid base64")
    void returnsEmptyWhenPayloadIsNotValidBase64()
    {
        final String jwtToken = "header.invalid-base64!.signature";

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Parses JWT token with empty payload object")
    void parsesJwtTokenWithEmptyPayloadObject()
    {
        final String payloadJson = "{}";
        final String jwtToken = buildTestJwtToken(payloadJson);

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    @DisplayName("Parses JWT token with array in payload")
    void parsesJwtTokenWithArrayInPayload()
    {
        final String payloadJson = "{\"roles\":[\"admin\",\"user\"]}";
        final String jwtToken = buildTestJwtToken(payloadJson);

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertTrue(result.isPresent());
        assertTrue(result.get().get("roles").isArray());
        assertEquals(2, result.get().get("roles").size());
        assertEquals("admin", result.get().get("roles").get(0).asText());
        assertEquals("user", result.get().get("roles").get(1).asText());
    }

    @Test
    @DisplayName("Parses JWT token with nested objects")
    void parsesJwtTokenWithNestedObjects()
    {
        final String payloadJson = "{\"user\":{\"id\":\"123\",\"name\":\"John\"}}";
        final String jwtToken = buildTestJwtToken(payloadJson);

        final Optional<JsonNode> result = JwtPayloadParser.parsePayload(jwtToken);

        assertTrue(result.isPresent());
        assertTrue(result.get().get("user").isObject());
        assertEquals("123", result.get().get("user").get("id").asText());
        assertEquals("John", result.get().get("user").get("name").asText());
    }

    private String buildTestJwtToken(final String payloadJson)
    {
        final String headerJson = "{\"alg\":\"none\"}";
        final String header = base64UrlEncode(headerJson);
        final String payload = base64UrlEncode(payloadJson);
        return header + "." + payload + ".signature";
    }

    @SuppressWarnings("SameParameterValue")
    private String buildTestJwtTokenWithoutPadding(final String payloadJson)
    {
        final String headerJson = "{\"alg\":\"none\"}";
        final String header = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        final String payload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    private String base64UrlEncode(final String value)
    {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
