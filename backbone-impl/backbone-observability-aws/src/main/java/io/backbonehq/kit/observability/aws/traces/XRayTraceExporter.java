package io.backbonehq.kit.observability.aws.traces;

import io.backbonehq.kit.http.aws.AwsSignedHttpRequest;
import io.backbonehq.kit.http.aws.AwsSignedHttpResponse;
import io.backbonehq.kit.http.aws.AwsSigningServiceName;
import io.backbonehq.kit.http.aws.SignedHttpTransport;
import io.backbonehq.kit.observability.aws.encode.OtlpTracePayloadEncoder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import org.jboss.logging.Logger;
import software.amazon.awssdk.http.SdkHttpMethod;

/**
 * Exports OpenTelemetry spans to the AWS X-Ray OTLP endpoint with SigV4 signing.
 * <p>
 * Not a CDI bean: created by {@link XRaySpanExporterProducer} at startup so OTel worker
 * threads never trigger lazy {@code @ConfigProperty} injection.
 */
public final class XRayTraceExporter implements SpanExporter
{
    private static final Logger LOGGER = Logger.getLogger(XRayTraceExporter.class);
    private static final String OTLP_PROTOBUF_CONTENT_TYPE = "application/x-protobuf";

    private final SignedHttpTransport transport;
    private final String otlpEndpoint;
    private final String awsRegion;

    XRayTraceExporter(final SignedHttpTransport transport, final String otlpEndpoint, final String awsRegion)
    {
        this.transport = transport;
        this.otlpEndpoint = otlpEndpoint;
        this.awsRegion = awsRegion;
    }

    @Override
    public CompletableResultCode export(final Collection<SpanData> spans)
    {
        if (spans.isEmpty())
        {
            return CompletableResultCode.ofSuccess();
        }
        try
        {
            final byte[] body = OtlpTracePayloadEncoder.encode(spans);
            final AwsSignedHttpResponse response = transport.send(buildRequest(body));
            if (response.isSuccessful())
            {
                return CompletableResultCode.ofSuccess();
            }

            logOtlpExportFailure(spans.size(), body.length, response);
            return CompletableResultCode.ofFailure();
        }
        catch (final RuntimeException exception)
        {
            LOGGER.error("X-Ray OTLP export failed", exception);
            return CompletableResultCode.ofFailure();
        }
    }

    private static void logOtlpExportFailure(final int spanCount, final int otlpBodyBytes, final AwsSignedHttpResponse response)
    {
        LOGGER.errorf(
            "X-Ray OTLP export failed with status %d otlpBodyBytes=%d spanCount=%d responseBody=%s",
            response.statusCode(),
            otlpBodyBytes,
            spanCount,
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

    private AwsSignedHttpRequest buildRequest(final byte[] body)
    {
        return new AwsSignedHttpRequest(
                                        SdkHttpMethod.POST,
                                        URI.create(otlpEndpoint),
                                        Map.of("Content-Type", OTLP_PROTOBUF_CONTENT_TYPE),
                                        body,
                                        AwsSigningServiceName.XRAY,
                                        awsRegion);
    }
}
