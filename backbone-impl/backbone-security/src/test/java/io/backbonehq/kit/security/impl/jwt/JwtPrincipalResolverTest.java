package io.backbonehq.kit.security.impl.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.backbonehq.kit.security.api.jwt.JwtPrincipal;
import io.backbonehq.kit.security.api.jwt.JwtPrincipalExtractor;
import jakarta.enterprise.inject.Instance;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtPrincipalResolverTest
{
    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    private final Instance<JwtPrincipalExtractor> extractors = mock(Instance.class);

    @Test
    @DisplayName("Returns first successful extraction from extractors")
    void returnsFirstSuccessfulExtractionFromExtractors()
    {
        final JwtPrincipalExtractor firstExtractor = mock(JwtPrincipalExtractor.class);
        final JwtPrincipalExtractor secondExtractor = mock(JwtPrincipalExtractor.class);
        final JwtPrincipal.Service servicePrincipal = new JwtPrincipal.Service("service-123");
        final JsonNode payload = mapper.createObjectNode().put("custom:service_id", "service-123");

        when(firstExtractor.extract(payload)).thenReturn(Optional.of(servicePrincipal));

        final List<JwtPrincipalExtractor> extractorList = List.of(firstExtractor, secondExtractor);
        when(extractors.spliterator()).thenReturn(extractorList.spliterator());

        final String jwtToken = buildTestJwtToken("{\"custom:service_id\":\"service-123\"}");
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken(jwtToken);

        assertTrue(result.isPresent());
        assertEquals(servicePrincipal, result.get());
    }

    @Test
    @DisplayName("Tries extractors in order until one succeeds")
    void triesExtractorsInOrderUntilOneSucceeds()
    {
        final JwtPrincipalExtractor firstExtractor = mock(JwtPrincipalExtractor.class);
        final JwtPrincipalExtractor secondExtractor = mock(JwtPrincipalExtractor.class);
        final JwtPrincipal.User userPrincipal = new JwtPrincipal.User("user@example.com");
        final JsonNode payload = mapper.createObjectNode().put("cognito:username", "user@example.com");

        when(firstExtractor.extract(payload)).thenReturn(Optional.empty());
        when(secondExtractor.extract(payload)).thenReturn(Optional.of(userPrincipal));

        final List<JwtPrincipalExtractor> extractorList = List.of(firstExtractor, secondExtractor);
        when(extractors.spliterator()).thenReturn(extractorList.spliterator());

        final String jwtToken = buildTestJwtToken("{\"cognito:username\":\"user@example.com\"}");
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken(jwtToken);

        assertTrue(result.isPresent());
        assertEquals(userPrincipal, result.get());
    }

    @Test
    @DisplayName("Returns empty when all extractors return empty")
    void returnsEmptyWhenAllExtractorsReturnEmpty()
    {
        final JwtPrincipalExtractor extractor = mock(JwtPrincipalExtractor.class);
        final JsonNode payload = mapper.createObjectNode().put("other_field", "value");

        when(extractor.extract(payload)).thenReturn(Optional.empty());

        final List<JwtPrincipalExtractor> extractorList = List.of(extractor);
        when(extractors.spliterator()).thenReturn(extractorList.spliterator());

        final String jwtToken = buildTestJwtToken("{\"other_field\":\"value\"}");
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken(jwtToken);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when no extractors are registered")
    void returnsEmptyWhenNoExtractorsAreRegistered()
    {
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken("header.payload.signature");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token cannot be parsed")
    void returnsEmptyWhenTokenCannotBeParsed()
    {
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken("invalid.token");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token is null")
    void returnsEmptyWhenTokenIsNull()
    {
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken(null);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Returns empty when token is blank")
    void returnsEmptyWhenTokenIsBlank()
    {
        final JwtPrincipalResolver resolver = new JwtPrincipalResolver(extractors);
        final Optional<JwtPrincipal> result = resolver.resolveFromToken("   ");

        assertFalse(result.isPresent());
    }

    private String buildTestJwtToken(final String payloadJson)
    {
        final String headerJson = "{\"alg\":\"none\"}";
        final String header = base64UrlEncode(headerJson);
        final String payload = base64UrlEncode(payloadJson);
        return header + "." + payload + ".signature";
    }

    private String base64UrlEncode(final String value)
    {
        return java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
