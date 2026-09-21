package io.backbonehq.kit.throttle.impl.reference;

import io.backbonehq.kit.security.api.rest.Secured;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
@IfBuildProfile("test")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class ReferenceTestResource
{
    @POST
    public Response test()
    {
        return Response.ok().build();
    }

    /**
     * <b>Note:</b> The {@code @Secured} annotation and authentication implementation are not implemented in the backbone-kit repo.
     * <p>
     * This means that while the tests call {@code /test/secured} (which is the correct semantic choice), the endpoint does not
     * enforce authentication. However, this does not affect the validity of these rate-limiting tests, as the rate limiting
     * filter extracts user identity from JWT headers independently of whether authentication is enforced by the endpoint.
     * The rate-limiting behavior is tested correctly regardless of the authentication enforcement status.
     */
    @POST
    @Secured
    @Path("/secured")
    public Response testSecured()
    {
        return Response.ok().build();
    }
}
