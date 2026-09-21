package io.backbonehq.kit.examples.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Example demonstrating automatic validation exception mapping.
 * 
 * <p>The {@code ValidationExceptionMapper} is automatically wired in
 * when backbone-common is included as a dependency. You only need to:
 * <ul>
 *   <li>Use Jakarta Bean Validation annotations on your DTOs</li>
 *   <li>Use {@code @Valid} annotation on method parameters</li>
 * </ul>
 * 
 * <p>When validation fails, the mapper automatically converts
 * {@code ConstraintViolationException} to a 400 Bad Request response
 * with all validation error messages.
 */
@Path("/api/examples")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ValidationExample
{
    /**
     * Example endpoint with validation.
     * 
     * <p>If the request body fails validation, a 400 Bad Request response
     * will be returned automatically with all validation errors:
     * 
     * <pre>
     * {
     *   "errors": [
     *     "Email must be valid",
     *     "Name must not be blank"
     *   ]
     * }
     * </pre>
     */
    @POST
    @Path("/users")
    public Response createUser(@Valid CreateUserRequest request)
    {
        // If we reach here, validation passed
        return Response.ok("User created: " + request.name()).build();
    }
}

/**
 * Example request DTO with validation constraints.
 * 
 * <p>When this DTO is validated, all constraint violations are collected
 * and returned in the error response.
 */
record CreateUserRequest(
    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    String email,

    @NotNull(message = "Age must not be null")
    Integer age
)
{
}
