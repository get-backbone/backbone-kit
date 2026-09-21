package io.backbonehq.kit.observability.aws.metrics;

import io.backbonehq.kit.http.aws.AwsSignedHttpRequest;
import io.backbonehq.kit.http.aws.AwsSignedHttpResponse;
import io.backbonehq.kit.http.aws.AwsSigningServiceName;
import io.backbonehq.kit.http.aws.SignedHttpTransport;
import io.backbonehq.kit.observability.api.encode.PrometheusRemoteWriteEncoder;
import io.backbonehq.kit.observability.api.encode.RemoteWritePayload;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.http.SdkHttpMethod;

/**
 * Pushes Micrometer Prometheus metrics to Amazon Managed Prometheus via remote write.
 */
@ApplicationScoped
public final class AmpMetricsExporter
{
    private static final Logger LOGGER = Logger.getLogger(AmpMetricsExporter.class);
    private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    private static final String REMOTE_WRITE_VERSION_HEADER = "X-Prometheus-Remote-Write-Version";
    private static final String SNAPPY_ENCODING = "snappy";

    @Inject
    PrometheusMeterRegistry prometheusMeterRegistry;

    @Inject
    PrometheusRemoteWriteEncoder encoder;

    @Inject
    SignedHttpTransport transport;

    @Inject
    MeterRegistry meterRegistry;

    @ConfigProperty(name = "backbone.observability.amp.remote-write.url")
    String remoteWriteUrl;

    @ConfigProperty(name = "aws.region")
    String awsRegion;

    AmpMetricsExporter()
    {
    }

    AmpMetricsExporter(
                       final PrometheusMeterRegistry prometheusMeterRegistry, final PrometheusRemoteWriteEncoder encoder, final SignedHttpTransport transport, final MeterRegistry meterRegistry, final String remoteWriteUrl, final String awsRegion)
    {
        this.prometheusMeterRegistry = prometheusMeterRegistry;
        this.encoder = encoder;
        this.transport = transport;
        this.meterRegistry = meterRegistry;
        this.remoteWriteUrl = remoteWriteUrl;
        this.awsRegion = awsRegion;
    }

    /**
     * Pushes the current metric snapshot to AMP on a fixed interval.
     */
    @Scheduled(
        every = "${backbone.observability.amp.push-interval}", skipExecutionIf = AmpRemoteWriteSkipPredicate.class)
    void pushMetrics()
    {
        final MetricSnapshots snapshots = prometheusMeterRegistry.getPrometheusRegistry().scrape();
        final RemoteWritePayload payload = encoder.encodeSnappy(snapshots);
        final AwsSignedHttpResponse response = transport.send(buildRequest(payload));
        if (response.isSuccessful())
        {
            meterRegistry.counter("backbone.observability.amp.push.success").increment();
            return;
        }
        meterRegistry.counter("backbone.observability.amp.push.failure").increment();
        logRemoteWriteFailure(snapshots.size(), payload.snappyBody().length, response);
    }

    private static void logRemoteWriteFailure(
                                              final int snapshotCount, final int snappyBodyBytes, final AwsSignedHttpResponse response)
    {
        LOGGER.errorf(
            "AMP remote write failed with status %d snappyBodyBytes=%d metricSnapshots=%d responseBody=%s",
            response.statusCode(),
            snappyBodyBytes,
            snapshotCount,
            formatResponseBody(response.body()));
    }

    private static String formatResponseBody(final byte[] body)
    {
        if (body.length == 0)
        {
            return "<empty>";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    private AwsSignedHttpRequest buildRequest(final RemoteWritePayload payload)
    {
        return new AwsSignedHttpRequest(
                                        SdkHttpMethod.POST,
                                        URI.create(remoteWriteUrl),
                                        Map.of(
                                            "Content-Type", payload.contentType(),
                                            CONTENT_ENCODING_HEADER, SNAPPY_ENCODING,
                                            REMOTE_WRITE_VERSION_HEADER, payload.remoteWriteVersion()),
                                        payload.snappyBody(),
                                        AwsSigningServiceName.APS,
                                        awsRegion);
    }
}
