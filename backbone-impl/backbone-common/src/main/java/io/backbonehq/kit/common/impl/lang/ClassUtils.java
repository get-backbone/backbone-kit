package io.backbonehq.kit.common.impl.lang;

public final class ClassUtils
{
    private ClassUtils()
    {
    }

    public static boolean exists(final String className)
    {
        try
        {
            Class.forName(className);
            return true;
        }
        catch (final ClassNotFoundException e)
        {
            return false;
        }
    }

    public static boolean existsWithClassLoader(final String className, @SuppressWarnings("rawtypes") final Object scope)
    {
        try
        {
            Class.forName(className, false, scope.getClass().getClassLoader());
            return true;
        }
        catch (final ClassNotFoundException e)
        {
            return false;
        }
    }
}
