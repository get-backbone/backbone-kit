package io.backbonehq.kit.common.impl.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Base64UtilsTest
{
    @Test
    @DisplayName("Adds padding to string without padding")
    void addsPaddingToUnpaddedString()
    {
        // Base64 URL encoded "test" without padding (length 6, needs 2 padding chars)
        final String unpadded = "dGVzdA";
        final String padded = Base64Utils.addBase64Padding(unpadded);

        assertEquals("dGVzdA==", padded);
        // Verify it can be decoded
        final byte[] decoded = Base64.getUrlDecoder().decode(padded);
        assertEquals("test", new String(decoded, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Adds two padding characters when needed")
    void addsTwoPaddingCharacters()
    {
        // Base64 URL encoded "te" without padding (length 2, needs 2 padding chars)
        final String unpadded = "dGU";
        final String padded = Base64Utils.addBase64Padding(unpadded);

        assertEquals("dGU=", padded);
    }

    @Test
    @DisplayName("Leaves already padded string unchanged")
    void leavesPaddedStringUnchanged()
    {
        final String alreadyPadded = "dGVzdA==";
        final String result = Base64Utils.addBase64Padding(alreadyPadded);

        assertEquals(alreadyPadded, result);
    }

    @Test
    @DisplayName("Handles string that is already multiple of 4")
    void handlesStringMultipleOfFour()
    {
        // Base64 URL encoded "test12" (length 8, already multiple of 4)
        final String multipleOfFour = "dGVzdDEy";
        final String result = Base64Utils.addBase64Padding(multipleOfFour);

        assertEquals(multipleOfFour, result);
    }

    @Test
    @DisplayName("Returns null for null input")
    void returnsNullForNullInput()
    {
        assertNull(Base64Utils.addBase64Padding(null));
    }

    @Test
    @DisplayName("Returns empty string for empty input")
    void returnsEmptyStringForEmptyInput()
    {
        assertEquals("", Base64Utils.addBase64Padding(""));
    }

    @Test
    @DisplayName("Handles single character input")
    void handlesSingleCharacter()
    {
        final String singleChar = "d";
        final String padded = Base64Utils.addBase64Padding(singleChar);

        assertEquals("d===", padded);
    }

    @Test
    @DisplayName("Handles JWT payload-like strings")
    void handlesJwtPayloadLikeStrings()
    {
        // Simulate a JWT payload that needs padding
        final String jwtPayload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
        final String padded = Base64Utils.addBase64Padding(jwtPayload);

        // Should add padding (original length 74, needs 2 padding chars to reach 76)
        assertEquals(jwtPayload + "==", padded);
    }
}
