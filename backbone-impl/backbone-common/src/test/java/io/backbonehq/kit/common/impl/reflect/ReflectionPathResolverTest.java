package io.backbonehq.kit.common.impl.reflect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReflectionPathResolver Tests")
class ReflectionPathResolverTest
{
    @Test
    @DisplayName("extractValueByPath with numeric index returns parameter value")
    void extractValueByPath_WithNumericIndex_ReturnsParameterValue() throws Exception
    {
        final String actorId = "actor-123";

        final Object result = ReflectionPathResolver.extractValueByPath("0", new Object[]{actorId});

        assertEquals(actorId, result, "Should return the parameter at index 0");
    }

    @Test
    @DisplayName("extractValueByPath with simple property access returns property value")
    void extractValueByPath_WithSimpleProperty_ReturnsPropertyValue() throws Exception
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final Object[] args = {user};

        final Object result = ReflectionPathResolver.extractValueByPath("0#username", args);

        assertEquals("john.doe", result, "Should return the username property");
    }

    @Test
    @DisplayName("extractValueByPath with nested property access returns nested value")
    void extractValueByPath_WithNestedProperty_ReturnsNestedValue() throws Exception
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final TestRequest request = new TestRequest(user, true);
        final Object[] args = {request};

        final Object result = ReflectionPathResolver.extractValueByPath("0#user#username", args);

        assertEquals("john.doe", result, "Should return the nested username property");
    }

    @Test
    @DisplayName("extractValueByPath with numeric index and nested property returns nested value")
    void extractValueByPath_WithNumericIndexAndNestedProperty_ReturnsNestedValue() throws Exception
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final TestRequest request = new TestRequest(user, true);
        final TestNestedRequest nestedRequest = new TestNestedRequest(request);
        final Object[] args = {nestedRequest};

        final Object result = ReflectionPathResolver.extractValueByPath("0#registerRequest#user#email", args);

        assertEquals("john@example.com", result, "Should return deeply nested email property");
    }

    @Test
    @DisplayName("extractValueByPath with boolean property using isGetter returns boolean value")
    void extractValueByPath_WithBooleanProperty_ReturnsBooleanValue() throws Exception
    {
        final TestRequest request = new TestRequest(null, true);
        final Object[] args = {request};

        final Object result = ReflectionPathResolver.extractValueByPath("0#active", args);

        assertEquals(true, result, "Should return the boolean property using isActive()");
    }

    @Test
    @DisplayName("extractValueByPath with null intermediate value returns null")
    void extractValueByPath_WithNullIntermediate_ReturnsNull() throws Exception
    {
        final TestRequest request = new TestRequest(null, true);
        final Object[] args = {request};

        final Object result = ReflectionPathResolver.extractValueByPath("0#user#username", args);

        assertNull(result, "Should return null when intermediate value is null");
    }

    @Test
    @DisplayName("extractValueByPath with invalid index throws IllegalArgumentException")
    void extractValueByPath_WithInvalidIndex_ThrowsIllegalArgumentException()
    {
        final Object[] args = {"value"};

        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionPathResolver.extractValueByPath("5", args),
            "Should throw IllegalArgumentException for out of bounds index"
        );

        assertEquals("Parameter index out of bounds: 5", exception.getMessage());
    }

    @Test
    @DisplayName("extractValueByPath with negative index throws IllegalArgumentException")
    void extractValueByPath_WithNegativeIndex_ThrowsIllegalArgumentException()
    {
        final Object[] args = {"value"};

        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionPathResolver.extractValueByPath("-1", args),
            "Should throw IllegalArgumentException for negative index"
        );

        assertEquals("Parameter index out of bounds: -1", exception.getMessage());
    }

    @Test
    @DisplayName("extractValueByPath with non-numeric parameter spec throws IllegalArgumentException")
    void extractValueByPath_WithNonNumericParameterSpec_ThrowsIllegalArgumentException()
    {
        final Object[] args = {"value"};

        final IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionPathResolver.extractValueByPath("unknownParam", args),
            "Should throw IllegalArgumentException for non-numeric parameter spec"
        );

        assertTrue(exception.getMessage().contains("Parameter spec must be a numeric index"),
            "Error message should indicate parameter spec must be numeric");
    }

    @Test
    @DisplayName("extractValueByPath with missing property throws NoSuchMethodException")
    void extractValueByPath_WithMissingProperty_ThrowsNoSuchMethodException()
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final Object[] args = {user};

        final Exception exception = assertThrows(
            Exception.class,
            () -> ReflectionPathResolver.extractValueByPath("0#nonexistent", args),
            "Should throw NoSuchMethodException for missing property"
        );

        assertTrue(
            exception instanceof NoSuchMethodException || (exception.getCause() != null && exception.getCause() instanceof NoSuchMethodException),
            "Should throw NoSuchMethodException for missing property"
        );
    }

    @Test
    @DisplayName("extractValueByPath with empty path throws IllegalArgumentException")
    void extractValueByPath_WithEmptyPath_ThrowsIllegalArgumentException()
    {
        final Object[] args = {"value"};

        final Exception exception = assertThrows(
            Exception.class,
            () -> ReflectionPathResolver.extractValueByPath("", args),
            "Should throw exception for empty path"
        );

        assertInstanceOf(IllegalArgumentException.class, exception, "Should throw IllegalArgumentException for empty path");
    }

    @Test
    @DisplayName("extractValueByPath with multiple parameters selects correct one by index")
    void extractValueByPath_WithMultipleParameters_SelectsCorrectByIndex() throws Exception
    {
        final String actorId = "actor-123";
        final String limit = "10";
        final Object[] args = {actorId, limit};

        final Object result = ReflectionPathResolver.extractValueByPath("1", args);

        assertEquals(limit, result, "Should return the second parameter (index 1)");
    }

    @Test
    @DisplayName("extractValueByPath with record accessor method name works")
    void extractValueByPath_WithDirectMethodName_WorksWhenGetterNotFound() throws Exception
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final Object[] args = {user};

        // Records use accessor methods like username() instead of getUsername()
        // ReflectionPathResolver tries: getUsername(), isUsername(), then username() (direct)
        // For records, the direct method name (username) should work
        final Object result = ReflectionPathResolver.extractValueByPath("0#username", args);

        assertEquals("john.doe", result, "Should work with record accessor method name");
    }

    @Test
    @DisplayName("extractValueByPath with # shorthand defaults to first parameter")
    void extractValueByPath_WithHashShorthand_DefaultsToFirstParameter() throws Exception
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final Object[] args = {user};

        // "#username" should default to index 0 (first parameter)
        final Object result = ReflectionPathResolver.extractValueByPath("#username", args);

        assertEquals("john.doe", result, "Should default to first parameter when path starts with #");
    }

    @Test
    @DisplayName("extractValueByPath with # shorthand and nested property works")
    void extractValueByPath_WithHashShorthandAndNestedProperty_Works() throws Exception
    {
        final TestUser user = new TestUser("john.doe", "john@example.com");
        final TestRequest request = new TestRequest(user, true);
        final Object[] args = {request};

        // "#user#username" should default to index 0 and navigate nested properties
        final Object result = ReflectionPathResolver.extractValueByPath("#user#username", args);

        assertEquals("john.doe", result, "Should default to first parameter and navigate nested properties");
    }
}
