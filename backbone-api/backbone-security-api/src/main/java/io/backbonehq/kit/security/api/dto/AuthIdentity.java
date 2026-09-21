package io.backbonehq.kit.security.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Domain entity representing an identity in the authentication system. This could be either an actor or a service identity (eg auth-service,
 * actor-service etc.)
 * <p>
 * This is a pure domain model with no external dependencies.
 */
@Schema(description = "Authenticated identity information from Cognito")
public record AuthIdentity(
                           @Schema(description = "Identity unique identifier (UUID from Cognito)", examples = "123e4567-e89b-12d3-a456-426614174000") String sub,
                           @Schema(description = "Username", examples =
                           {
                               "johndoe", "document-service"}) String username,
                           @Schema(description = "Email address", examples = "john.doe@example.com") String email,
                           @Schema(description = "Identity roles") List<String> roles,
                           @Schema(description = "Token expiration timestamp") Instant tokenExpiration
){
    @JsonCreator
    public AuthIdentity(@JsonProperty("sub") final String sub, @JsonProperty("username") final String username, @JsonProperty("email") final String email, @JsonProperty("roles") final List<String> roles, @JsonProperty("tokenExpiration") final Instant tokenExpiration)
    {
        this.sub = sub;
        this.username = username;
        this.email = email;
        this.roles = roles == null ? null : List.copyOf(roles);
        this.tokenExpiration = tokenExpiration;
    }

    public boolean isTokenExpired()
    { return tokenExpiration != null && Instant.now().isAfter(tokenExpiration); }

    public boolean hasRole(final String role)
    {
        return roles != null && roles.contains(role);
    }
}
