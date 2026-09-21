package io.backbonehq.kit.metrics.impl.persistence.recorder;

import io.backbonehq.kit.metrics.api.persistence.MetricsRecord;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public final class DatabaseMetricsRecorder
{

    @Inject
    MeterRegistry meterRegistry;

    /**
     * Records a database operation duration.
     *
     * @param operation The database operation (e.g., "save", "findByCandidateId", "delete")
     * @param entity    The entity type class
     * @param duration  The duration in milliseconds
     */
    public void recordDatabaseOperation(final String operation, final Class<? extends MetricsRecord> entity, final long duration)
    {
        final String entityLabel = MetricsRecord.metricsLabelFor(entity);

        Timer.builder("database.operation.duration")
            .tag("operation", operation)
            .tag("entity", entityLabel)
            .description("Duration of database operations")
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
}
