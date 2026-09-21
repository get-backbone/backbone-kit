package io.backbonehq.kit.security.api.rest;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark JAX-RS resource methods that require user authentication. The
 * {@link UserTokenAuthorizationInterceptor} will
 * verify that a user has been authenticated by the {@link TokenAuthenticationFilter} before allowing the method to
 * execute.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Secured
{
}
