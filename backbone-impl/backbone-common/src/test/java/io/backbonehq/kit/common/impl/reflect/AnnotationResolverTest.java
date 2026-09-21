package io.backbonehq.kit.common.impl.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnnotationResolver}.
 */
class AnnotationResolverTest
{
    private InvocationContext context;

    @BeforeEach
    void setUp()
    {
        context = mock(InvocationContext.class);
    }

    @Test
    void resolve_ReturnsMethodAnnotation_WhenPresent()
    {
        final Method method = TestReflectionHelper.createTestMethod("testMethod");
        when(context.getMethod()).thenReturn(method);

        final TestAnnotation result = AnnotationResolver.resolve(context, TestAnnotation.class);

        assertNotNull(result);
        assertEquals("method", result.value());
    }

    @Test
    void resolve_ReturnsClassAnnotation_WhenMethodHasNone()
    {
        final Method method = TestReflectionHelper.createTestMethod("testMethod", String.class);
        when(context.getMethod()).thenReturn(method);

        final TestAnnotation result = AnnotationResolver.resolve(context, TestAnnotation.class);

        assertNotNull(result);
        assertEquals("class", result.value());
    }

    @Test
    void resolve_ReturnsNull_WhenNeitherMethodNorClassHasAnnotation() throws NoSuchMethodException
    {
        final Method method = TestUser.class.getMethod("username");
        when(context.getMethod()).thenReturn(method);

        final TestAnnotation result = AnnotationResolver.resolve(context, TestAnnotation.class);

        assertNull(result);
    }
}
