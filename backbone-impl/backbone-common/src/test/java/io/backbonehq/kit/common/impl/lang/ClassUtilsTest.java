package io.backbonehq.kit.common.impl.lang;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClassUtilsTest
{
    @Test
    void exists_WithExistingJavaLangClass_ReturnsTrue()
    {
        final boolean result = ClassUtils.exists("java.lang.String");

        assertTrue(result, "Should return true for existing java.lang.String class");
    }

    @Test
    void exists_WithExistingProjectClass_ReturnsTrue()
    {
        final boolean result = ClassUtils.exists(ClassUtils.class.getCanonicalName());

        assertTrue(result, "Should return true for existing ClassUtils class");
    }

    @Test
    void exists_WithInnerClass_ReturnsTrue()
    {
        final boolean result = ClassUtils.exists("java.util.Map$Entry");

        assertTrue(result, "Should return true for existing inner class");
    }

    @Test
    void exists_WithArrayClass_ReturnsTrue()
    {
        final boolean result = ClassUtils.exists("[Ljava.lang.String;");

        assertTrue(result, "Should return true for array class");
    }

    @Test
    void exists_WithNonExistingClass_ReturnsFalse()
    {
        final boolean result = ClassUtils.exists("com.example.NonExistentClass");

        assertFalse(result, "Should return false for non-existing class");
    }

    @Test
    void exists_WithInvalidClassName_ReturnsFalse()
    {
        final boolean result = ClassUtils.exists("Invalid.Class.Name.123");

        assertFalse(result, "Should return false for invalid class name");
    }

    @Test
    void exists_WithEmptyString_ReturnsFalse()
    {
        final boolean result = ClassUtils.exists("");

        assertFalse(result, "Should return false for empty string");
    }

    @Test
    void exists_WithNull_ThrowsNullPointerException()
    {
        assertThrows(
            NullPointerException.class,
            () -> ClassUtils.exists(null),
            "Should throw NullPointerException for null input"
        );
    }
}
