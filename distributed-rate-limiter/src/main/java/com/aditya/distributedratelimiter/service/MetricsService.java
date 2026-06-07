package com.aditya.distributedratelimiter.service;

import com.aditya.distributedratelimiter.semantics.MetricsSemantics;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;


@Service
public class MetricsService {

  private final LongCounter totalRequests;

  private final DoubleHistogram redisLatency;

  // Cache (status, strategy) -> Attributes to avoid per-request allocations on a hot path.
  private final Map<String, Attributes> totalRequestAttributesCache = new ConcurrentHashMap<>();

  public MetricsService(OpenTelemetry openTelemetry) {
    Meter meter = openTelemetry.meterBuilder("distributed-rate-limiter").build();

    totalRequests = meter.counterBuilder(MetricsSemantics.Metrics.TOTAL_REQUESTS.getName())
        .setDescription(MetricsSemantics.Metrics.TOTAL_REQUESTS.getDescription())
        .setUnit(MetricsSemantics.Metrics.TOTAL_REQUESTS.getUnit())
        .build();

    redisLatency = meter.histogramBuilder(MetricsSemantics.Metrics.REDIS_LATENCY.getName())
        .setDescription(MetricsSemantics.Metrics.REDIS_LATENCY.getDescription())
        .setUnit(MetricsSemantics.Metrics.REDIS_LATENCY.getUnit())
        .build();
  }

  public void recordRejectedRequest(String strategy) {
    totalRequests.add(1, totalRequestAttributes(MetricsSemantics.STATUS.REJECTED, strategy));
  }

  public void recordAllowedRequest(String strategy) {
    totalRequests.add(1, totalRequestAttributes(MetricsSemantics.STATUS.ALLOWED, strategy));
  }

  public void recordRedisLatency(double latencyMs) {
    redisLatency.record(latencyMs);
  }

  private Attributes totalRequestAttributes(String status, String strategy) {
    String cacheKey = status + '|' + strategy;
    return totalRequestAttributesCache.computeIfAbsent(
        cacheKey,
        unused -> Attributes.of(
            MetricsSemantics.Attributes.STATUS.getAttributeKey(), status,
            MetricsSemantics.Attributes.STRATEGY.getAttributeKey(), strategy));
  }

}
