package io.backbonehq.kit.observability.aws.traces;

import io.backbonehq.kit.http.aws.SignedHttpTransport;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Registers {@link XRayTraceExporter} with Quarkus OpenTelemetry when X-Ray export is enabled.
 */
@ApplicationScoped
public final class XRaySpanExporterProducer
{
    private static final Logger LOGGER = Logger.getLogger(XRaySpanExporterProducer.class);

    private static final SpanExporter DISABLED = new SpanExporter()
    {
        @Override
        public CompletableResultCode export(final Collection<SpanData> spans)
        {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush()
        {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown()
        {
            return CompletableResultCode.ofSuccess();
        }
    };

    @Inject
    SignedHttpTransport transport;

    @ConfigProperty(name = "backbone.observability.xray.export.enabled")
    boolean exportEnabled;

    @ConfigProperty(name = "backbone.observability.xray.otlp.endpoint")
    String otlpEndpoint;

    @ConfigProperty(name = "aws.region")
    String awsRegion;

    /**
     * Supplies the X-Ray span exporter for {@code quarkus.otel.traces.exporter=cdi}.
     */
    @Produces
    @Singleton
    SpanExporter xraySpanExporter()
    {
        if (exportEnabled)
        {
            LOGGER.infof("X-Ray OTLP span export enabled: endpoint=%s region=%s", otlpEndpoint, awsRegion);
            return new XRayTraceExporter(transport, otlpEndpoint, awsRegion);
        }

        LOGGER.info("X-Ray OTLP span export is disabled; using no-op SpanExporter");
        return DISABLED;
    }
}
