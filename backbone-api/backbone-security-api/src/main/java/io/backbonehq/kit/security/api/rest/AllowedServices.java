package io.backbonehq.kit.security.api.rest;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to restrict JAX-RS resource methods to specific services.
 * The {@link io.backbonehq.security.infrastructure.http.interceptor.ServiceTokenAuthorizationInterceptor} will verify that the calling service
 * is in the allowed list before allowing the method to execute.
 *
 * <p>This annotation can be used on methods or classes. When used on a class, it applies to all methods in that class.
 *
 * <p>Example usage at method level:
 * <pre>
 * {@code
 * @Path("/api/parse")
 * public class ParseResource
 * {
 *
 * @POST
 * @Secured
 *          @AllowedServices({"document-service"})
 *          public Response parseDocument(ParseRequest request) {
 *          // Only document-service can access this endpoint
 *          return Response.ok().build();
 *          }
 *          }
 *          }
 *          </pre>
 *
 *          <p>Example usage at class level (applies to all methods):
 *          <pre>
 *          {@code
 *          @Path("/api/documents")
 * @Secured
 *          @AllowedServices({"document-service", "backend-actor"})
 *          public class DocumentResource {
 * @GET
 *      @Path("/{id}")
 *      public Response getDocument(@PathParam("id") String id) {
 *      // All methods in this class are restricted to document-service or backend-actor
 *      return Response.ok().build();
 *      }
 *      }
 *      }
 *      </pre>
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AllowedServices
{
    /**
     * List of service names that are allowed to access this endpoint.
     * Service names should match the service identifier used in service authentication.
     *
     * @return array of allowed service names
     */
    String[] value();
}
