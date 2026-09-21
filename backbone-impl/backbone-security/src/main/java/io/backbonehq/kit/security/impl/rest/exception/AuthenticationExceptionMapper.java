package io.backbonehq.kit.security.impl.rest.exception;

import io.backbonehq.kit.security.api.domain.exception.AuthenticationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

/**
 * JAX-RS exception mapper that converts {@link AuthenticationException} to a 401 Unauthorized HTTP response.
 */
@Provider
public final class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException>
{
    private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(AuthenticationExceptionMapper.class);

    @Override
    public Response toResponse(final AuthenticationException exception)
    {
        LOGGER.debug("Authentication failed: " + exception.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", exception.getMessage()))
            .build();
    }
}
