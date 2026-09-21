package io.backbonehq.kit.common.impl.lang;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for Base64 URL encoding/decoding operations.
 * <p>
 * Provides helper methods for working with Base64 URL-safe encoding, particularly
 * for JWT token processing where padding may be omitted.
 */
public final class Base64Utils
{
    private Base64Utils()
    {
    }

    /**
     * Adds Base64 padding ('=') to a Base64 URL-encoded string if needed.
     * <p>
     * Base64 URL encoding (used in JWT tokens) may omit padding. This method ensures
     * the string has proper padding for decoding with standard Base64 decoders.
     * <p>
     * The target length is calculated as the next multiple of 4, padding with '=' characters.
     *
     * @param payload the Base64 URL-encoded string (may be without padding)
     * @return the padded string, or the original string if already properly padded
     */
    public static String addBase64Padding(final String payload)
    {
        if (StringUtils.isNotBlank(payload))
        {
            final int targetLength = (payload.length() + 3) / 4 * 4;
            return StringUtils.rightPad(payload, targetLength, '=');
        }
        else
        {
            return payload;
        }
    }
}
