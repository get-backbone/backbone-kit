package io.backbonehq.kit.common.impl.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * JAX-RS exception mapper that converts {@link ConstraintViolationException} to a 400 Bad Request HTTP response.
 * Extracts all validation error messages and returns them in a structured format.
 */
@Provider
public final class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException>
{
    private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(final ConstraintViolationException exception)
    {
        final List<String> errors = exception.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        LOGGER.debugf("Validation failed: %s", errors);

        // Return all error messages
        final List<String> errorMessages = errors.isEmpty() ? List.of("Validation failed") : errors;

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("errors", errorMessages))
            .build();
    }
}
