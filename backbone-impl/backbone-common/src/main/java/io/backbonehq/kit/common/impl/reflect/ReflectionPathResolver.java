package io.backbonehq.kit.common.impl.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public final class ReflectionPathResolver
{
    private ReflectionPathResolver()
    {}

    public static Object extractValueByPath(final String path, final Object[] methodArgs) throws Exception
    {
        if (path == null || path.trim().isEmpty())
        {
            throw new IllegalArgumentException("Empty property path");
        }

        final String[] parts = path.split("#");
        if (parts.length == 0)
        {
            throw new IllegalArgumentException("Empty property path");
        }

        // If path starts with "#", default to index 0 (first parameter)
        // "#username" splits to ["", "username"] - use "0" for the parameter
        // "0#username" splits to ["0", "username"] - use "0" for the parameter
        // "1#username" splits to ["1", "username"] - use "1" for the parameter
        // Empty string "" splits to [""] - this is invalid, should be caught above
        final String paramSpec = parts[0].isEmpty() ? "0" : parts[0];
        Object current = resolveParameter(paramSpec, methodArgs);

        // Start property navigation from index 1 (skip the parameter spec)
        for (int i = 1; i < parts.length; i++)
        {
            if (current == null) return null;
            current = getPropertyValue(current, parts[i]);
        }

        return current;
    }

    private static Object resolveParameter(final String paramSpec, final Object[] args)
    {
        try
        {
            final int index = Integer.parseInt(paramSpec);
            if (index >= 0 && index < args.length)
            {
                return args[index];
            }
            else
            {
                throw new IllegalArgumentException("Parameter index out of bounds: " + index);
            }
        }
        catch (final NumberFormatException e)
        {
            throw new IllegalArgumentException("Parameter spec must be a numeric index (e.g., \"0\", \"1\") or start with \"#\" for first parameter, got: " + paramSpec);
        }
    }

    private static Object getPropertyValue(final Object obj, final String propertyName) throws Exception
    {
        final Class<?> clazz = obj.getClass();
        final String[] candidateMethods = {"get" + StringUtils.capitalize(propertyName), "is" + StringUtils.capitalize(propertyName), propertyName
        };

        final Method getter = Arrays.stream(candidateMethods)
            .map(name ->
            {
                try
                {
                    return clazz.getMethod(name);
                }
                catch (final NoSuchMethodException ignored)
                {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new NoSuchMethodException(
                                                         "Property accessor not found for '" + propertyName + "' on " + clazz.getSimpleName()
            ));

        assert getter != null;
        return getter.invoke(obj);
    }
}
