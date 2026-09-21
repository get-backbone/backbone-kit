package io.backbonehq.kit.security.api.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;

public interface JwtPrincipalExtractor
{
    Optional<JwtPrincipal> extract(final JsonNode payload);
}
