package io.backbonehq.kit.observability.api.encode;

import io.backbonehq.kit.observability.api.remotewrite.Label;
import io.backbonehq.kit.observability.api.remotewrite.Sample;
import io.backbonehq.kit.observability.api.remotewrite.TimeSeries;
import io.backbonehq.kit.observability.api.remotewrite.WriteRequest;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.xerial.snappy.Snappy;

/**
 * Encodes Prometheus {@link MetricSnapshots} into PRW 1.0 {@link WriteRequest} payloads.
 *
 * <p>Backbone uses Micrometer's {@code micrometer-registry-prometheus} with Prometheus client v1.x
 * ({@code quarkus-micrometer-registry-prometheus-v1}). Samples are read via
 * {@code PrometheusMeterRegistry.getPrometheusRegistry().scrape()} — not via text scrape parsing.
 *
 * <p>Remote-write pushes stamp every sample with a single wall-clock time for the batch. Per-datapoint
 * scrape timestamps from the registry are not forwarded, avoiding AMP {@code new-value-for-timestamp}
 * discards on periodic push.
 *
 * <p>PMD class cyclomatic complexity is suppressed: this class dispatches many small type-specific
 * appenders; individual methods remain intentionally small.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
@ApplicationScoped
public final class PrometheusRemoteWriteEncoder
{
    private static final String NAME_LABEL = "__name__";
    private static final String POSITIVE_INFINITY = "+Inf";

    /**
     * Encodes metric snapshots into a remote-write request.
     */
    public WriteRequest encode(final MetricSnapshots metricSnapshots)
    {
        final long pushTimestampMillis = System.currentTimeMillis();
        final WriteRequest.Builder requestBuilder = WriteRequest.newBuilder();
        for (final MetricSnapshot metricSnapshot : metricSnapshots)
        {
            appendSnapshot(metricSnapshot, requestBuilder, pushTimestampMillis);
        }
        return requestBuilder.build();
    }

    /**
     * Encodes and Snappy-compresses metric snapshots for AMP remote-write POST bodies.
     */
    public RemoteWritePayload encodeSnappy(final MetricSnapshots metricSnapshots)
    {
        final byte[] protobufBody = encode(metricSnapshots).toByteArray();
        try
        {
            final byte[] compressedBody = Snappy.compress(protobufBody);
            return new RemoteWritePayload(
                                          compressedBody,
                                          RemoteWritePayload.DEFAULT_CONTENT_TYPE,
                                          RemoteWritePayload.DEFAULT_REMOTE_WRITE_VERSION);
        }
        catch (final IOException exception)
        {
            throw new UncheckedIOException("Failed to Snappy-compress remote-write payload", exception);
        }
    }

    private void appendSnapshot(final MetricSnapshot metricSnapshot, final WriteRequest.Builder requestBuilder, final long pushTimestampMillis)
    {
        // Use traditional instanceof + cast instead of pattern-matching switch to avoid Clover instrumentation issues.
        if (metricSnapshot instanceof CounterSnapshot counterSnapshot)
        {
            appendCounter(counterSnapshot, requestBuilder, pushTimestampMillis);
        }
        else if (metricSnapshot instanceof GaugeSnapshot gaugeSnapshot)
        {
            appendGauge(gaugeSnapshot, requestBuilder, pushTimestampMillis);
        }
        else if (metricSnapshot instanceof HistogramSnapshot histogramSnapshot)
        {
            appendHistogram(histogramSnapshot, requestBuilder, pushTimestampMillis);
        }
        else if (metricSnapshot instanceof SummarySnapshot summarySnapshot)
        {
            appendSummary(summarySnapshot, requestBuilder, pushTimestampMillis);
        }
    }

    private void appendCounter(final CounterSnapshot counterSnapshot, final WriteRequest.Builder requestBuilder, final long pushTimestampMillis)
    {
        final String metricName = counterSeriesName(counterSnapshot.getMetadata().getPrometheusName());
        for (final CounterSnapshot.CounterDataPointSnapshot dataPoint : counterSnapshot.getDataPoints())
        {
            requestBuilder.addTimeseries(singleSampleSeries(metricName, dataPoint, dataPoint.getValue(), pushTimestampMillis));
        }
    }

    private void appendGauge(final GaugeSnapshot gaugeSnapshot, final WriteRequest.Builder requestBuilder, final long pushTimestampMillis)
    {
        final String metricName = gaugeSnapshot.getMetadata().getPrometheusName();
        for (final GaugeSnapshot.GaugeDataPointSnapshot dataPoint : gaugeSnapshot.getDataPoints())
        {
            requestBuilder.addTimeseries(singleSampleSeries(metricName, dataPoint, dataPoint.getValue(), pushTimestampMillis));
        }
    }

    private void appendHistogram(final HistogramSnapshot histogramSnapshot, final WriteRequest.Builder requestBuilder, final long pushTimestampMillis)
    {
        final String metricName = histogramSnapshot.getMetadata().getPrometheusName();
        for (final HistogramSnapshot.HistogramDataPointSnapshot dataPoint : histogramSnapshot.getDataPoints())
        {
            if (!dataPoint.hasClassicHistogramData())
            {
                continue;
            }
            appendClassicHistogram(metricName, dataPoint, requestBuilder, pushTimestampMillis);
        }
    }

    private void appendClassicHistogram(final String metricName, final HistogramSnapshot.HistogramDataPointSnapshot dataPoint, final WriteRequest.Builder requestBuilder, final long pushTimestampMillis)
    {
        final ClassicHistogramBuckets buckets = dataPoint.getClassicBuckets();
        for (int bucketIndex = 0; bucketIndex < buckets.size(); bucketIndex++)
        {
            final String bucketName = metricName + "_bucket";
            final String upperBound = formatUpperBound(buckets.getUpperBound(bucketIndex));
            final List<Label> labels = buildBucketLabels(bucketName, dataPoint.getLabels(), upperBound);
            requestBuilder.addTimeseries(TimeSeries.newBuilder()
                .addAllLabels(labels)
                .addSamples(sample(buckets.getCount(bucketIndex), pushTimestampMillis))
                .build());
        }
        appendSumAndCount(
            requestBuilder,
            metricName,
            buildLabels(metricName, dataPoint.getLabels()),
            dataPoint.getSum(),
            dataPoint.getCount(),
            pushTimestampMillis);
    }

    private void appendSummary(final SummarySnapshot summarySnapshot, final WriteRequest.Builder requestBuilder, final long pushTimestampMillis)
    {
        final String metricName = summarySnapshot.getMetadata().getPrometheusName();
        for (final SummarySnapshot.SummaryDataPointSnapshot dataPoint : summarySnapshot.getDataPoints())
        {
            for (int quantileIndex = 0; quantileIndex < dataPoint.getQuantiles().size(); quantileIndex++)
            {
                final Quantile quantile = dataPoint.getQuantiles().get(quantileIndex);
                final List<Label> labels = buildQuantileLabels(
                    metricName, dataPoint.getLabels(), quantile.getQuantile());
                requestBuilder.addTimeseries(TimeSeries.newBuilder()
                    .addAllLabels(labels)
                    .addSamples(sample(quantile.getValue(), pushTimestampMillis))
                    .build());
            }
            appendSumAndCount(
                requestBuilder,
                metricName,
                buildLabels(metricName, dataPoint.getLabels()),
                dataPoint.getSum(),
                dataPoint.getCount(),
                pushTimestampMillis);
        }
    }

    private void appendSumAndCount(final WriteRequest.Builder requestBuilder, final String metricName, final List<Label> baseLabels, final double sum, final double count, final long timestampMillis)
    {
        requestBuilder.addTimeseries(TimeSeries.newBuilder()
            .addAllLabels(replaceNameLabel(baseLabels, metricName + "_sum"))
            .addSamples(sample(sum, timestampMillis))
            .build());
        requestBuilder.addTimeseries(TimeSeries.newBuilder()
            .addAllLabels(replaceNameLabel(baseLabels, metricName + "_count"))
            .addSamples(sample(count, timestampMillis))
            .build());
    }

    private TimeSeries singleSampleSeries(final String metricName, final DataPointSnapshot dataPoint, final double value, final long pushTimestampMillis)
    {
        return TimeSeries.newBuilder()
            .addAllLabels(buildLabels(metricName, dataPoint.getLabels()))
            .addSamples(sample(value, pushTimestampMillis))
            .build();
    }

    private Sample sample(final double value, final long timestampMillis)
    {
        return Sample.newBuilder().setValue(value).setTimestamp(timestampMillis).build();
    }

    private String counterSeriesName(final String prometheusName)
    {
        return prometheusName.endsWith("_total") ? prometheusName : prometheusName + "_total";
    }

    private String formatUpperBound(final double upperBound)
    {
        if (Double.isInfinite(upperBound) && upperBound > 0)
        {
            return POSITIVE_INFINITY;
        }
        return Double.toString(upperBound);
    }

    private List<Label> replaceNameLabel(final List<Label> labels, final String metricName)
    {
        return labels.stream()
            .map(label -> NAME_LABEL.equals(label.getName()) ? Label.newBuilder().setName(NAME_LABEL).setValue(metricName).build() : label)
            .toList();
    }

    private List<Label> buildLabels(final String metricName, final Labels dataPointLabels)
    {
        return buildLabels(metricName, dataPointLabels, List.of(), List.of());
    }

    private List<Label> buildBucketLabels(
                                          final String metricName, final Labels dataPointLabels, final String upperBound)
    {
        return buildLabels(metricName, dataPointLabels, List.of("le"), List.of(upperBound));
    }

    private List<Label> buildQuantileLabels(
                                            final String metricName, final Labels dataPointLabels, final double quantile)
    {
        return buildLabels(
            metricName, dataPointLabels, List.of("quantile"), List.of(Double.toString(quantile)));
    }

    private List<Label> buildLabels(
                                    final String metricName, final Labels dataPointLabels, final List<String> extraLabelNames, final List<String> extraLabelValues)
    {
        final List<Label> labels = new ArrayList<>(dataPointLabels.size() + extraLabelNames.size() + 1);
        labels.add(Label.newBuilder().setName(NAME_LABEL).setValue(metricName).build());
        for (int index = 0; index < dataPointLabels.size(); index++)
        {
            labels.add(Label.newBuilder()
                .setName(dataPointLabels.getPrometheusName(index))
                .setValue(dataPointLabels.getValue(index))
                .build());
        }
        for (int index = 0; index < extraLabelNames.size(); index++)
        {
            labels.add(Label.newBuilder()
                .setName(extraLabelNames.get(index))
                .setValue(extraLabelValues.get(index))
                .build());
        }
        labels.sort(Comparator.comparing(Label::getName));
        return labels;
    }
}
