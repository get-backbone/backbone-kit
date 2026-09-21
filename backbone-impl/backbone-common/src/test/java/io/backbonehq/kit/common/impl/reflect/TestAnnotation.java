package io.backbonehq.kit.common.impl.reflect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Runtime annotation for reflect tests (e.g. {@link AnnotationResolverTest}).
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotation
{
    String value() default "";
}
