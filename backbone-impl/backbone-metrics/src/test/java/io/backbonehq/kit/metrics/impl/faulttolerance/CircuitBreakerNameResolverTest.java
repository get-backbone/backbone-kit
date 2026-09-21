package io.backbonehq.kit.metrics.impl.faulttolerance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.smallrye.faulttolerance.api.CircuitBreakerName;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerNameResolverTest
{
    private final InvocationContext context = mock(InvocationContext.class);

    @Test
    @DisplayName("resolve returns method annotation value when present")
    void resolve_ReturnsMethodAnnotationValue_WhenPresent() throws Exception
    {
        final Method method = TestTarget.class.getMethod("methodWithCircuitBreakerName");
        when(context.getMethod()).thenReturn(method);

        final String result = CircuitBreakerNameResolver.resolve(context);

        assertEquals("method-level-name", result);
    }

    @Test
    @DisplayName("resolve returns fully qualified class name with method name when no annotation")
    void resolve_ReturnsFullyQualifiedClassNameWithMethodName_WhenNoAnnotation() throws Exception
    {
        final Method method = NoAnnotationTarget.class.getMethod("someMethod");
        when(context.getMethod()).thenReturn(method);

        final String result = CircuitBreakerNameResolver.resolve(context);

        assertEquals(NoAnnotationTarget.class.getName() + "#someMethod", result);
    }

    @Test
    @DisplayName("resolve returns fully qualified class name when method annotation is empty")
    void resolve_ReturnsFullyQualifiedClassName_WhenMethodAnnotationIsEmpty() throws Exception
    {
        final Method method = TestTarget.class.getMethod("methodWithEmptyCircuitBreakerName");
        when(context.getMethod()).thenReturn(method);

        final String result = CircuitBreakerNameResolver.resolve(context);

        assertEquals(TestTarget.class.getName() + "#methodWithEmptyCircuitBreakerName", result);
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class TestTarget
    {
        @CircuitBreakerName("method-level-name")
        public void methodWithCircuitBreakerName()
        {
        }

        @CircuitBreakerName("")
        public void methodWithEmptyCircuitBreakerName()
        {
        }

        public void methodWithoutCircuitBreakerName()
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
