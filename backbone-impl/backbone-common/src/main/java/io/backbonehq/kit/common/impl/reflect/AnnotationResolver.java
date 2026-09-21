package io.backbonehq.kit.common.impl.reflect;

import jakarta.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class AnnotationResolver
{
    private AnnotationResolver()
    {

    }

    public static <A extends Annotation> A resolve(final InvocationContext context, final Class<A> annotationClass)
    {
        final Method method = context.getMethod();

        // method-level first
        final A onMethod = method.getAnnotation(annotationClass);
        if (onMethod != null)
        {
            return onMethod;
        }

        // class-level fallback
        return method.getDeclaringClass().getAnnotation(annotationClass);
    }
}
