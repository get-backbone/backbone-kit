package io.backbonehq.kit.security.impl.infrastructure;

import io.backbonehq.kit.security.api.rest.AllowedServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.interceptor.InvocationContext;

/**
 * Resolves allowed services from method or class annotations.
 */
@ApplicationScoped
public final class AllowedServicesResolver
{
    /**
     * Gets the allowed services annotation from the method or class.
     *
     * @param context The invocation context
     * @return The annotation, or null if not found
     */
    public AllowedServices getAnnotation(final InvocationContext context)
    {
        final AllowedServices methodAnnotation = context.getMethod().getAnnotation(AllowedServices.class);
        if (methodAnnotation != null)
        {
            return methodAnnotation;
        }

        return context.getTarget().getClass().getAnnotation(AllowedServices.class);
    }

    /**
     * Gets the allowed services array from the annotation.
     *
     * @param annotation The annotation
     * @return The allowed services array, or empty array if annotation is null or has no values
     */
    public String[] getAllowedServices(final AllowedServices annotation)
    {
        if (annotation == null)
        {
            return new String[0];
        }

        final String[] services = annotation.value();
        return services.length == 0 ? new String[0] : services;
    }
}
