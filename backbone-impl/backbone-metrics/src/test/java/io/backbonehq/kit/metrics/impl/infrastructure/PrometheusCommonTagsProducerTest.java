package io.backbonehq.kit.metrics.impl.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PrometheusCommonTagsProducerTest
{
    @Test
    void prometheusCommonTags_addsServiceAndInstanceToMeters() throws Exception
    {
        final PrometheusCommonTagsProducer producer = new PrometheusCommonTagsProducer();
        setField(producer, "applicationName", "auth-service");
        setField(producer, "configuredInstance", Optional.of("test-instance"));

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        registry.config().meterFilter(producer.prometheusCommonTags());
        Counter.builder("probe_total").register(registry).increment();

        final Meter.Id meterId = registry.get("probe_total").counter().getId();
        assertEquals("auth-service", meterId.getTag(PrometheusCommonTagsProducer.SERVICE_TAG));
        assertEquals("test-instance", meterId.getTag(PrometheusCommonTagsProducer.INSTANCE_TAG));
    }

    @Test
    void resolveInstanceTag_usesHostNameWhenNotConfigured() throws Exception
    {
        final PrometheusCommonTagsProducer producer = new PrometheusCommonTagsProducer();
        setField(producer, "applicationName", "actor-service");
        setField(producer, "configuredInstance", Optional.empty());

        final SimpleMeterRegistry registry = new SimpleMeterRegistry();
        registry.config().meterFilter(producer.prometheusCommonTags());
        Counter.builder("probe_total").register(registry).increment();

        final String instanceTag = registry.get("probe_total").counter().getId()
            .getTag(PrometheusCommonTagsProducer.INSTANCE_TAG);
        assertNotNull(instanceTag);
        assertEquals(MetricsHostName.localHostName(), instanceTag);
    }

    private static void setField(final Object target, final String fieldName, final Object value) throws Exception
    {
        final Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
