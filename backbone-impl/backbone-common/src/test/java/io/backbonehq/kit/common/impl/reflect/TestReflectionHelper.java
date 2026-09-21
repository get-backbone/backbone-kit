package io.backbonehq.kit.common.impl.reflect;

import java.lang.reflect.Method;

public final class TestReflectionHelper
{
    /**
     * Helper method to create a test method with specified parameter types.
     */
    public static Method createTestMethod(final String methodName, final Class<?>... paramTypes)
    {
        try
        {
            return TestMethodClass.class.getDeclaredMethod(methodName, paramTypes);
        }
        catch (final NoSuchMethodException e)
        {
            throw new RuntimeException("Failed to create test method", e);
        }
    }
}
