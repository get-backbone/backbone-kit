package io.backbonehq.kit.common.impl.inject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.enterprise.inject.Instance;
import java.util.Optional;
import java.util.function.Consumer;
import javax.naming.ServiceUnavailableException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("unchecked")
class FluentCdiResolverTest
{
    // -----------------------
    // mapOrElse
    // -----------------------
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void mapOrElse_BehavesCorrectly(boolean resolvable)
    {
        final String beanValue = "bean";
        final Instance<String> instance = mockInstance(beanValue, resolvable);

        final String result = FluentCdiResolver.of(instance)
            .mapOrElse(
                b -> "mapped",
                () -> "fallback"
            );

        assertEquals(resolvable ? "mapped" : "fallback", result);
    }

    // -----------------------
    // ifPresent
    // -----------------------
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ifPresent_BehavesCorrectly(boolean resolvable)
    {
        final String beanValue = "bean";
        final Instance<String> instance = mockInstance(beanValue, resolvable);

        Consumer<String> action = mock(Consumer.class);

        FluentCdiResolver.of(instance).ifPresent(action);

        if (resolvable)
        {
            verify(action).accept(beanValue);
        }
        else
        {
            verify(action, never()).accept(any());
        }
    }

    // -----------------------
    // ifPresentOrElse
    // -----------------------
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ifPresentOrElse_BehavesCorrectly(boolean resolvable)
    {
        final String beanValue = "bean";
        final Instance<String> instance = mockInstance(beanValue, resolvable);

        Consumer<String> action = mock(Consumer.class);
        Runnable alternative = mock(Runnable.class);

        FluentCdiResolver.of(instance).ifPresentOrElse(action, alternative);

        if (resolvable)
        {
            verify(action).accept(beanValue);
            verify(alternative, never()).run();
        }
        else
        {
            verify(action, never()).accept(any());
            verify(alternative).run();
        }
    }

    // -----------------------
    // orElseThrow
    // -----------------------
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void orElseThrow_BehavesCorrectly(boolean resolvable)
    {
        final String beanValue = "bean";
        final Instance<String> instance = mockInstance(beanValue, resolvable);

        if (resolvable)
        {
            try
            {
                String result = FluentCdiResolver.of(instance)
                    .orElseThrow(() -> new ServiceUnavailableException("fail"));
                assertEquals(beanValue, result);
            }
            catch (ServiceUnavailableException e)
            {
                fail("Exception should not be thrown for resolvable instance");
            }
        }
        else
        {
            ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> FluentCdiResolver.of(instance)
                    .orElseThrow(() -> new ServiceUnavailableException("fail"))
            );
            assertEquals("fail", exception.getMessage());
        }
    }

    // -----------------------
    // asOptional
    // -----------------------
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void asOptional_BehavesCorrectly(boolean resolvable)
    {
        final String beanValue = "bean";
        final Instance<String> instance = mockInstance(beanValue, resolvable);

        Optional<String> result = FluentCdiResolver.of(instance).asOptional();

        if (resolvable)
        {
            assertTrue(result.isPresent());
            assertEquals(beanValue, result.get());
        }
        else
        {
            assertFalse(result.isPresent());
        }
    }

    // -----------------------
    // Helper to create mocks
    // -----------------------
    private <T> Instance<T> mockInstance(@SuppressWarnings("SameParameterValue") T value, boolean resolvable)
    {
        final Instance<T> instance = mock(Instance.class);

        when(instance.isResolvable()).thenReturn(resolvable);

        if (resolvable) when(instance.get()).thenReturn(value);

        return instance;
    }
}
