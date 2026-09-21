package io.backbonehq.kit.common.impl.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationExceptionMapperTest
{
    private final ValidationExceptionMapper mapper = new ValidationExceptionMapper();

    @Test
    @DisplayName("Returns 400 with multiple error messages")
    void testMultipleErrors()
    {
        final ConstraintViolationException exception = createException("Username required", "Password required");
        final Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        final List<String> errors = extractErrors(response);

        assertEquals(2, errors.size());
        assertTrue(errors.contains("Username required"));
        assertTrue(errors.contains("Password required"));
    }

    @Test
    @DisplayName("Returns 400 with single error message")
    void testSingleError()
    {
        final ConstraintViolationException exception = createException("Email must be valid");
        final Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        final List<String> errors = extractErrors(response);

        assertEquals(1, errors.size());
        assertEquals("Email must be valid", errors.getFirst());
    }

    @Test
    @DisplayName("Returns 400 with default message when violations are empty")
    void testEmptyViolations()
    {
        final ConstraintViolationException exception = createException(); // no messages
        final Response response = mapper.toResponse(exception);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        final List<String> errors = extractErrors(response);

        assertEquals(1, errors.size());
        assertEquals("Validation failed", errors.getFirst());
    }

    private ConstraintViolationException createException(String... messages)
    {
        final Set<ConstraintViolation<?>> violations = new HashSet<>();
        for (final String message : messages)
        {
            final ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn(message);
            violations.add(violation);
        }

        return new ConstraintViolationException("Validation failed", violations);
    }

    private List<String> extractErrors(Response response)
    {
        @SuppressWarnings("unchecked") final Map<String, List<String>> entity = (Map<String, List<String>>) response.getEntity();

        return entity.get("errors");
    }
}
