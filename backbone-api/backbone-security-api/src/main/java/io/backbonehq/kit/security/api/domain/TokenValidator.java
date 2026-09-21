package io.backbonehq.kit.security.api.domain;

import io.backbonehq.kit.security.api.dto.TokenValidation;

/**
 * Domain provider interface for JWT token validation.
 * Validates JWT tokens and extracts user information for authentication.
 *
 * <p>This interface validates both user tokens (from actor pool) and service tokens (from service pool).
 * The validation result indicates which type of token was validated.
 */
public interface TokenValidator
{

    /**
     * Validate a JWT token and extract user information.
     *
     * @param jwtToken the JWT token to validate
     * @return token validation result with user info if valid
     */
    TokenValidation validateToken(String jwtToken);
}
