package io.backbonehq.kit.common.impl.reflect;

/**
 * Test class for method reflection. Used by {@link TestReflectionHelper} and annotation-resolution tests.
 */
@TestAnnotation("class")
class TestMethodClass
{
    @TestAnnotation("method")
    public void testMethod()
    {
    }

    public void testMethod(final String param)
    {
    }

    public void testMethod(final TestUser user)
    {
    }

    public void testMethod(final TestRequest request)
    {
    }

    public void testMethod(final String param1, final String param2)
    {
    }

    public void testMethod(final String param1, final int param2)
    {
    }

    public void testMethod(final TestUser user, final int limit)
    {
    }

    public void testMethod(final String actorId, final String limit, final String other)
    {
    }
}
