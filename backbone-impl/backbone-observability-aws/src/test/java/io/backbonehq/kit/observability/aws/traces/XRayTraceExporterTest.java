package io.backbonehq.kit.observability.aws.traces;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.backbonehq.kit.http.aws.AwsSignedHttpRequest;
import io.backbonehq.kit.http.aws.AwsSignedHttpResponse;
import io.backbonehq.kit.http.aws.AwsSigningServiceName;
import io.backbonehq.kit.http.aws.SignedHttpTransport;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpMethod;

class XRayTraceExporterTest
{
    @Test
    void export_sendsOtlpProtobufToXRay()
    {
        final AtomicReference<AwsSignedHttpRequest> capturedRequest = new AtomicReference<>();
        final SignedHttpTransport transport = request ->
        {
            capturedRequest.set(request);
            return new AwsSignedHttpResponse(200, new byte[0]);
        };

        final XRayTraceExporter exporter = new XRayTraceExporter(
                                                                 transport,
                                                                 "https://xray.us-west-2.amazonaws.com/v1/traces",
                                                                 "us-west-2");
        final TestSpanData span = TestSpanData.builder()
            .setName("auth-login")
            .setKind(SpanKind.SERVER)
            .setStatus(StatusData.ok())
            .setStartEpochNanos(1_000L)
            .setEndEpochNanos(2_000L)
            .setHasEnded(true)
            .setResource(Resource.create(io.opentelemetry.api.common.Attributes.of(
                AttributeKey.stringKey("service.name"), "auth-service")))
            .build();

        final CompletableResultCode result = exporter.export(List.of(span));

        assertTrue(result.isSuccess());
        final AwsSignedHttpRequest request = capturedRequest.get();
        assertEquals(SdkHttpMethod.POST, request.method());
        assertEquals(AwsSigningServiceName.XRAY, request.signingService());
        assertEquals("application/x-protobuf", request.headers().get("Content-Type"));
        assertTrue(request.body().length > 0);
    }

    @Test
    void export_whenNoSpans_doesNotSend()
    {
        final AtomicReference<AwsSignedHttpRequest> capturedRequest = new AtomicReference<>();
        final SignedHttpTransport transport = request ->
        {
            capturedRequest.set(request);
            return new AwsSignedHttpResponse(200, new byte[0]);
        };

        final XRayTraceExporter exporter = new XRayTraceExporter(transport, "https://xray.us-west-2.amazonaws.com/v1/traces", "us-west-2");

        final CompletableResultCode result = exporter.export(List.of());

        assertTrue(result.isSuccess());
        assertNull(capturedRequest.get());
    }

    @Test
    void export_logsOtlpBodySizeAndResponseBodyOnFailure()
    {
        final byte[] responseBody = "AccessDenied".getBytes(StandardCharsets.UTF_8);
        final SignedHttpTransport transport = request -> new AwsSignedHttpResponse(403, responseBody);
        final XRayTraceExporter exporter = new XRayTraceExporter(transport, "https://xray.us-west-2.amazonaws.com/v1/traces", "us-west-2");
        final TestSpanData span = TestSpanData.builder()
            .setName("auth-login")
            .setKind(SpanKind.SERVER)
            .setStatus(StatusData.ok())
            .setStartEpochNanos(1_000L)
            .setEndEpochNanos(2_000L)
            .setHasEnded(true)
            .setResource(Resource.create(io.opentelemetry.api.common.Attributes.of(
                AttributeKey.stringKey("service.name"), "auth-service")))
            .build();
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
        final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(XRayTraceExporter.class.getName());
        julLogger.addHandler(logHandler);

        try
        {
            final CompletableResultCode result = exporter.export(List.of(span));

            assertTrue(result.isDone());
            assertFalse(result.isSuccess());
            assertTrue(loggedMessage.get().contains("status 403"));
            assertTrue(loggedMessage.get().contains("otlpBodyBytes="));
            assertTrue(loggedMessage.get().contains("spanCount=1"));
            assertTrue(loggedMessage.get().contains("AccessDenied"));
        }
        finally
        {
            julLogger.removeHandler(logHandler);
        }
    }
}
