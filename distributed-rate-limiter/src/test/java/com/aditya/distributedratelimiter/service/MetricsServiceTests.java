package com.aditya.distributedratelimiter.service;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.aditya.distributedratelimiter.semantics.MetricsSemantics;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class MetricsServiceTests {

  private InMemoryMetricReader metricReader;
  private MetricsService metricsService;

  @BeforeEach
  void setUp() {
    metricReader = InMemoryMetricReader.create();
    metricsService = new MetricsService(OpenTelemetrySdk.builder()
        .setMeterProvider(SdkMeterProvider.builder().registerMetricReader(metricReader).build())
        .build());
  }

  @Test
  void shouldRecordAllowedRequestWithStatusAndStrategy() {
    metricsService.recordAllowedRequest(MetricsSemantics.STRATEGY.FIXED_WINDOW);

    assertThat(totalRequests())
        .hasName(MetricsSemantics.Metrics.TOTAL_REQUESTS.getName())
        .hasLongSumSatisfying(sum -> sum.hasPointsSatisfying(point -> point
            .hasValue(1)
            .hasAttribute(MetricsSemantics.Attributes.STATUS.getAttributeKey(),
                MetricsSemantics.STATUS.ALLOWED)
            .hasAttribute(MetricsSemantics.Attributes.STRATEGY.getAttributeKey(),
                MetricsSemantics.STRATEGY.FIXED_WINDOW)));
  }

  @Test
  void shouldRecordRejectedRequestWithStatusAndStrategy() {
    metricsService.recordRejectedRequest(MetricsSemantics.STRATEGY.SLIDING_WINDOW);

    assertThat(totalRequests())
        .hasLongSumSatisfying(sum -> sum.hasPointsSatisfying(point -> point
            .hasValue(1)
            .hasAttribute(MetricsSemantics.Attributes.STATUS.getAttributeKey(),
                MetricsSemantics.STATUS.REJECTED)
            .hasAttribute(MetricsSemantics.Attributes.STRATEGY.getAttributeKey(),
                MetricsSemantics.STRATEGY.SLIDING_WINDOW)));
  }

  @Test
  void shouldKeepSeparateSeriesPerStatusAndStrategy() {
    metricsService.recordAllowedRequest(MetricsSemantics.STRATEGY.FIXED_WINDOW);
    metricsService.recordAllowedRequest(MetricsSemantics.STRATEGY.FIXED_WINDOW);
    metricsService.recordAllowedRequest(MetricsSemantics.STRATEGY.SLIDING_WINDOW);
    metricsService.recordRejectedRequest(MetricsSemantics.STRATEGY.SLIDING_WINDOW);

    assertThat(totalRequests())
        .hasLongSumSatisfying(sum -> sum.hasPointsSatisfying(
            p -> p.hasValue(2)
                .hasAttribute(MetricsSemantics.Attributes.STATUS.getAttributeKey(),
                    MetricsSemantics.STATUS.ALLOWED)
                .hasAttribute(MetricsSemantics.Attributes.STRATEGY.getAttributeKey(),
                    MetricsSemantics.STRATEGY.FIXED_WINDOW),
            p -> p.hasValue(1)
                .hasAttribute(MetricsSemantics.Attributes.STATUS.getAttributeKey(),
                    MetricsSemantics.STATUS.ALLOWED)
                .hasAttribute(MetricsSemantics.Attributes.STRATEGY.getAttributeKey(),
                    MetricsSemantics.STRATEGY.SLIDING_WINDOW),
            p -> p.hasValue(1)
                .hasAttribute(MetricsSemantics.Attributes.STATUS.getAttributeKey(),
                    MetricsSemantics.STATUS.REJECTED)
                .hasAttribute(MetricsSemantics.Attributes.STRATEGY.getAttributeKey(),
                    MetricsSemantics.STRATEGY.SLIDING_WINDOW)));
  }

  @Test
  void shouldRecordRedisLatencyHistogram() {
    metricsService.recordRedisLatency(2.5);
    metricsService.recordRedisLatency(7.5);

    assertThat(metric(MetricsSemantics.Metrics.REDIS_LATENCY.getName()))
        .hasHistogramSatisfying(h -> h.hasPointsSatisfying(point -> point
            .hasCount(2)
            .hasSum(10.0)
            .hasMin(2.5)
            .hasMax(7.5)));
  }

  private MetricData totalRequests() {
    return metric(MetricsSemantics.Metrics.TOTAL_REQUESTS.getName());
  }

  private MetricData metric(String name) {
    return metricReader.collectAllMetrics().stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Metric not found: " + name));
  }
}
