package io.backbonehq.kit.security.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import io.backbonehq.kit.security.api.rest.AllowedServices;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AllowedServicesResolverTest
{
    private final InvocationContext context = mock(InvocationContext.class, RETURNS_DEEP_STUBS);

    @Test
    @DisplayName("getAnnotation returns method annotation when present")
    void getAnnotation_ReturnsMethodAnnotation_WhenPresent() throws Exception
    {
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        when(context.getMethod()).thenReturn(method);
        // getTarget() is not called when method annotation is present, so use lenient
        lenient().when(context.getTarget()).thenReturn(new TestTarget());

        final AllowedServices annotation = new AllowedServicesResolver().getAnnotation(context);

        assertArrayEquals(new String[]{"service-1", "service-2"}, annotation.value());
    }

    @Test
    @DisplayName("getAnnotation returns class annotation when method annotation not present")
    void getAnnotation_ReturnsClassAnnotation_WhenMethodAnnotationNotPresent() throws Exception
    {
        final Method method = ClassAnnotatedTarget.class.getMethod("someMethod");
        when(context.getMethod()).thenReturn(method);
        when(context.getTarget()).thenReturn(new ClassAnnotatedTarget());

        final AllowedServices annotation = new AllowedServicesResolver().getAnnotation(context);

        assertArrayEquals(new String[]{"class-service"}, annotation.value());
    }

    @Test
    @DisplayName("getAnnotation returns null when no annotation present")
    void getAnnotation_ReturnsNull_WhenNoAnnotationPresent() throws Exception
    {
        final Method method = NoAnnotationTarget.class.getMethod("someMethod");
        when(context.getMethod()).thenReturn(method);
        when(context.getTarget()).thenReturn(new NoAnnotationTarget());

        final AllowedServices annotation = new AllowedServicesResolver().getAnnotation(context);

        assertNull(annotation);
    }

    @Test
    @DisplayName("getAllowedServices returns empty array when annotation is null")
    void getAllowedServices_ReturnsEmptyArray_WhenAnnotationIsNull()
    {
        final String[] result = new AllowedServicesResolver().getAllowedServices(null);

        assertArrayEquals(new String[0], result);
    }

    @Test
    @DisplayName("getAllowedServices returns services array when annotation has values")
    void getAllowedServices_ReturnsServicesArray_WhenAnnotationHasValues() throws Exception
    {
        final Method method = TestTarget.class.getMethod("methodWithAnnotation");
        when(context.getMethod()).thenReturn(method);
        // getTarget() is not called when method annotation is present, so use lenient
        lenient().when(context.getTarget()).thenReturn(new TestTarget());
        final AllowedServices annotation = new AllowedServicesResolver().getAnnotation(context);

        final String[] result = new AllowedServicesResolver().getAllowedServices(annotation);

        assertArrayEquals(new String[]{"service-1", "service-2"}, result);
    }

    @Test
    @DisplayName("getAllowedServices returns empty array when annotation has empty array")
    void getAllowedServices_ReturnsEmptyArray_WhenAnnotationHasEmptyArray() throws Exception
    {
        final Method method = TestTarget.class.getMethod("methodWithEmptyAnnotation");
        when(context.getMethod()).thenReturn(method);
        // getTarget() is not called when method annotation is present, so use lenient
        lenient().when(context.getTarget()).thenReturn(new TestTarget());
        final AllowedServices annotation = new AllowedServicesResolver().getAnnotation(context);

        final String[] result = new AllowedServicesResolver().getAllowedServices(annotation);

        assertArrayEquals(new String[0], result);
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestTarget
    {
        @AllowedServices({"service-1", "service-2"})
        public void methodWithAnnotation()
        {
        }

        @AllowedServices({})
        public void methodWithEmptyAnnotation()
        {
        }

        public void methodWithoutAnnotation()
        {
        }
    }

    @AllowedServices({"class-service"})
    @SuppressWarnings("unused")
    static class ClassAnnotatedTarget
    {
        public void someMethod()
        {
        }
    }

    @SuppressWarnings("unused")
    static class NoAnnotationTarget
    {
        public void someMethod()
        {
        }
    }
}
