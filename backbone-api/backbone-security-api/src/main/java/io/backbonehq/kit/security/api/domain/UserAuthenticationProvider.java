package io.backbonehq.kit.security.api.domain;

import io.backbonehq.kit.security.api.dto.AuthResponse;

/**
 * Interface for authenticating users via AWS Cognito User Pools. Handles email/password authentication and user
 * management. Works with both AWS Cognito (production) and LocalStack (development).
 */
public interface UserAuthenticationProvider
{

    /**
     * Authenticate a user with username and password.
     *
     * @param username the username or email
     * @param password the password
     * @return authentication result containing tokens and user info
     */
    AuthResponse login(final String username, final String password);

    /**
     * Register a new user in the User Pool.
     *
     * @param email    the user email
     * @param password the password
     * @return authentication result if registration is successful
     */
    AuthResponse register(final String email, final String password);

    /**
     * Refresh access token using refresh token.
     *
     * @param username     optional username for SECRET_HASH calculation (if null, will try to extract from idToken or refreshToken)
     * @param idToken      optional ID token (JWT) to extract username from if username is not provided
     * @param refreshToken the refresh token
     * @return authentication result with new tokens
     */
    AuthResponse refreshUserToken(final String username, final String idToken, final String refreshToken);

    /**
     * Delete a user from the User Pool.
     * Used for compensating transactions when registration partially fails.
     *
     * @param username the username or email of the user to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteUser(final String username);
}
