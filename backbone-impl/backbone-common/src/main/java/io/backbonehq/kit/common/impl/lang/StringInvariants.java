package io.backbonehq.kit.common.impl.lang;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public final class StringInvariants
{
    private StringInvariants()
    {}

    /**
     * Returns true if all provided strings are non-blank and equal according to case-insensitive comparison.
     */
    public static boolean allEqualIgnoreCase(final String first, final String... rest)
    {
        if (StringUtils.isBlank(first))
            return false;

        for (final String value : rest)
        {
            if (StringUtils.isBlank(value) || !Strings.CI.equals(first, value))
                return false;
        }

        return true;
    }
}
