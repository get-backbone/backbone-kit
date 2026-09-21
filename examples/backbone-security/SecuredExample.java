package io.backbonehq.kit.examples.security;

import io.backbonehq.kit.security.api.rest.AllowedServices;
import io.backbonehq.kit.security.api.rest.Secured;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Example REST resource demonstrating security annotations usage.
 * 
 * This example shows how to use {@code @Secured} and {@code @AllowedServices}
 * annotations to enforce authentication and service-level authorization.
 * 
 * <p><strong>Note:</strong> The actual authentication filters and interceptors
 * must be implemented by your application. The annotations provided by
 * backbone-security are interceptor bindings that require corresponding
 * interceptors to be implemented.
 */
@Path("/api/examples")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SecuredExample
{
    /**
     * Public endpoint - no authentication required.
     * Demonstrates unsecured endpoint pattern.
     */
    @GET
    @Path("/public")
    public Response getPublicData()
    {
        return Response.ok("Public data").build();
    }

    /**
     * Secured endpoint - requires user authentication.
     * Demonstrates {@code @Secured} annotation usage.
     * 
     * <p>This endpoint requires a valid user token to be present in the request.
     * The authentication is enforced by an interceptor that must be implemented
     * by your application.
     */
    @GET
    @Path("/secured")
    @Secured
    public Response getSecuredData()
    {
        // User is automatically authenticated via @Secured annotation
        // Authentication is handled by your application's authentication interceptor
        return Response.ok("Secured data").build();
    }

    /**
     * Service-only endpoint - requires service authentication.
     * Demonstrates {@code @AllowedServices} annotation for service-level authorization.
     * 
     * <p>This endpoint requires both user authentication ({@code @Secured}) and
     * service-level authorization. Only the specified services can access this endpoint.
     */
    @POST
    @Path("/process")
    @Secured
    @AllowedServices({"processing-service"})
    public Response processData(ExampleRequest request)
    {
        // Only processing-service can call this endpoint
        // Service authentication is handled automatically by your application's interceptor
        return Response.ok("Processed").build();
    }

    /**
     * Multi-service endpoint - allows multiple services.
     * Demonstrates {@code @AllowedServices} with multiple service names.
     */
    @GET
    @Path("/shared/{id}")
    @Secured
    @AllowedServices({"service-a", "service-b", "api-gateway"})
    public Response getSharedData(@PathParam("id") String id)
    {
        // Any of the listed services can access this endpoint
        return Response.ok("Shared data for: " + id).build();
    }

    /**
     * Class-level security - all methods in class are secured.
     * Demonstrates applying {@code @Secured} at class level.
     */
    @Path("/admin")
    @Secured
    public static class AdminResource
    {
        @GET
        @Path("/users")
        public Response getUsers()
        {
            // All methods in this class require authentication
            return Response.ok("Users list").build();
        }

        @GET
        @Path("/stats")
        public Response getStats()
        {
            // Also requires authentication
            return Response.ok("Statistics").build();
        }
    }
}

/**
 * Example request DTO (generic, non-domain-specific).
 */
record ExampleRequest(String data, String type)
{
}
