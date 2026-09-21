package io.backbonehq.kit.common.impl.inject;

import static java.util.Objects.requireNonNull;

import jakarta.enterprise.inject.Instance;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fluent CDI resolver for conditional operations on Instance beans.
 */
public final class FluentCdiResolver<T>
{
    private final Instance<T> instance;

    private FluentCdiResolver(final Instance<T> instance)
    {
        this.instance = requireNonNull(instance, "instance cannot be null");
    }

    /**
     * Entry point to create a resolver for a CDI instance.
     */
    public static <T> FluentCdiResolver<T> of(final Instance<T> instance)
    {
        return new FluentCdiResolver<>(instance);
    }

    /**
     * Map the resolved bean to a result if resolvable, otherwise return fallback.
     */
    public <R> R mapOrElse(final Function<T, R> mapper, final Supplier<R> fallback)
    {
        requireNonNull(mapper, "mapper cannot be null");
        requireNonNull(fallback, "fallback cannot be null");

        return instance.isResolvable() ? mapper.apply(instance.get()) : fallback.get();
    }

    /**
     * Execute an action if the instance is resolvable.
     */
    public FluentCdiResolver<T> ifPresent(final Consumer<T> action)
    {
        requireNonNull(action, "action cannot be null");

        if (instance.isResolvable())
        {
            action.accept(instance.get());
        }

        return this;
    }

    /**
     * Execute an action if the instance is resolvable, otherwise execute alternative action.
     */
    public FluentCdiResolver<T> ifPresentOrElse(final Consumer<T> action, final Runnable alternativeAction)
    {
        requireNonNull(action, "action cannot be null");
        requireNonNull(alternativeAction, "alternativeAction cannot be null");

        if (instance.isResolvable())
        {
            action.accept(instance.get());
        }
        else
        {
            alternativeAction.run();
        }

        return this;
    }

    /**
     * Return the resolved bean or throw an exception provided by supplier.
     */
    public <E extends Throwable> T orElseThrow(final Supplier<E> exceptionSupplier) throws E
    {
        requireNonNull(exceptionSupplier, "exceptionSupplier cannot be null");

        if (!instance.isResolvable())
        {
            throw exceptionSupplier.get();
        }

        return instance.get();
    }

    /**
     * Return the resolved bean as an Optional.
     */
    public Optional<T> asOptional()
    {
        return instance.isResolvable() ? Optional.of(instance.get()) : Optional.empty();
    }
}
