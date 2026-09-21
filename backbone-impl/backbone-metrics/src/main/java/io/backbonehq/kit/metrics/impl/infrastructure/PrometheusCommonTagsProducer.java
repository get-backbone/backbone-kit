package io.backbonehq.kit.metrics.impl.infrastructure;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Adds {@code service} and {@code instance} tags to every Micrometer meter so workloads
 * exporting to a shared AMP workspace do not collide on binders such as {@code jvm_*}.
 */
@ApplicationScoped
public final class PrometheusCommonTagsProducer
{
    static final String SERVICE_TAG = "service";
    static final String INSTANCE_TAG = "instance";

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @ConfigProperty(name = "backbone.metrics.instance")
    Optional<String> configuredInstance;

    /**
     * Registers common Prometheus tags for all meter registries.
     */
    @Produces
    @Singleton
    MeterFilter prometheusCommonTags()
    {
        return MeterFilter.commonTags(commonTags());
    }

    private List<Tag> commonTags()
    {
        return List.of(
            Tag.of(SERVICE_TAG, applicationName),
            Tag.of(INSTANCE_TAG, resolveInstanceTag()));
    }

    private String resolveInstanceTag()
    {
        return configuredInstance
            .filter(value -> !value.isBlank())
            .orElseGet(MetricsHostName::localHostName);
    }
}
