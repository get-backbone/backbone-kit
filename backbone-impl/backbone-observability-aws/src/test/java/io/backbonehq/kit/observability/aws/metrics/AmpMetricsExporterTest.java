package io.backbonehq.kit.observability.aws.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.backbonehq.kit.http.aws.AwsSignedHttpRequest;
import io.backbonehq.kit.http.aws.AwsSignedHttpResponse;
import io.backbonehq.kit.http.aws.AwsSigningServiceName;
import io.backbonehq.kit.http.aws.SignedHttpTransport;
import io.backbonehq.kit.observability.api.encode.PrometheusRemoteWriteEncoder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpMethod;

class AmpMetricsExporterTest
{
    @Test
    void pushMetrics_sendsSnappyRemoteWriteToAmp()
    {
        final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        registry.counter("backbone_probe_total").increment();
        final AtomicReference<AwsSignedHttpRequest> capturedRequest = new AtomicReference<>();
        final SignedHttpTransport transport = request ->
        {
            capturedRequest.set(request);
            return new AwsSignedHttpResponse(204, new byte[0]);
        };
        final SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        final AmpMetricsExporter exporter = new AmpMetricsExporter(
                                                                   registry,
                                                                   new PrometheusRemoteWriteEncoder(),
                                                                   transport,
                                                                   metrics,
                                                                   "https://aps-workspaces.us-west-2.amazonaws.com/workspaces/ws-123/api/v1/remote_write",
                                                                   "us-west-2");

        exporter.pushMetrics();

        final AwsSignedHttpRequest request = capturedRequest.get();
        assertEquals(SdkHttpMethod.POST, request.method());
        assertEquals(AwsSigningServiceName.APS, request.signingService());
        assertEquals("us-west-2", request.region());
        assertEquals("application/x-protobuf", request.headers().get("Content-Type"));
        assertEquals("snappy", request.headers().get("Content-Encoding"));
        assertTrue(request.body().length > 0);
        assertEquals(1.0, metrics.get("backbone.observability.amp.push.success").counter().count());
    }

    @Test
    void pushMetrics_logsSnappySizeAndResponseBodyOnFailure()
    {
        final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        registry.counter("backbone_probe_total").increment();
        final byte[] responseBody = "sample-too-old".getBytes(StandardCharsets.UTF_8);
        final SignedHttpTransport transport = request -> new AwsSignedHttpResponse(400, responseBody);
        final SimpleMeterRegistry metrics = new SimpleMeterRegistry();
        final AmpMetricsExporter exporter = new AmpMetricsExporter(
                                                                   registry,
                                                                   new PrometheusRemoteWriteEncoder(),
                                                                   transport,
                                                                   metrics,
                                                                   "https://aps-workspaces.us-west-2.amazonaws.com/workspaces/ws-123/api/v1/remote_write",
                                                                   "us-west-2");
        final AtomicReference<String> loggedMessage = new AtomicReference<>();
        final Handler logHandler = new Handler()
        {
            @Override
            public void publish(final LogRecord record)
            {
                if (Level.SEVERE.equals(record.getLevel()))
                {
                    loggedMessage.set(record.getMessage());
                }
            }

            @Override
            public void flush()
            {
            }

            @Override
            public void close()
            {
            }
        };
        final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(
            "io.backbonehq.kit.observability.aws.metrics.AmpMetricsExporter");
        julLogger.addHandler(logHandler);

        try
        {
            exporter.pushMetrics();
        }
        finally
        {
            julLogger.removeHandler(logHandler);
        }

        assertEquals(1.0, metrics.get("backbone.observability.amp.push.failure").counter().count());
        assertTrue(loggedMessage.get().contains("status 400"));
        assertTrue(loggedMessage.get().contains("snappyBodyBytes="));
        assertTrue(loggedMessage.get().contains("metricSnapshots="));
        assertTrue(loggedMessage.get().contains("sample-too-old"));
    }
}
