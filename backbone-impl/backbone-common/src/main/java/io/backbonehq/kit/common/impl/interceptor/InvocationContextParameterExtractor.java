package io.backbonehq.kit.common.impl.interceptor;

import io.backbonehq.kit.common.impl.reflect.ReflectionPathResolver;
import jakarta.interceptor.InvocationContext;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;

public final class InvocationContextParameterExtractor
{
    private InvocationContextParameterExtractor()
    {}

    public static Object[] extractParameterValues(final InvocationContext context, final String[] argPaths)
    {
        return ArrayUtils.isNotEmpty(argPaths) ? extractByPaths(context, argPaths) : extractByIndices(context,
            new int[]{0});
    }

    private static Object[] extractByIndices(final InvocationContext context, final int[] indices)
    {
        final Object[] args = context.getParameters();
        return Arrays.stream(indices)
            .mapToObj(index -> (index >= 0 && index < args.length) ? args[index] : "<invalid index>")
            .toArray();
    }

    private static Object[] extractByPaths(final InvocationContext context, final String[] paths)
    {
        final Object[] args = context.getParameters();

        return Arrays.stream(paths)
            .map(path ->
            {
                try
                {
                    return ReflectionPathResolver.extractValueByPath(path, args);
                }
                catch (Exception e)
                {
                    return "<error: " + e.getMessage() + ">";
                }
            }).toArray();
    }
}
