package com.aditya.distributedratelimiter.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenTelemetryConfig {

  @Bean
  public OpenTelemetry opentelemetry() {
    // Logs metrics to stdout on every export interval. Swap for OtlpGrpcMetricExporter
    // once an OTel Collector / Prometheus stack is running on localhost:4317.
    MetricExporter metricExporter = LoggingMetricExporter.create();

    SdkMeterProvider meterProvider = SdkMeterProvider.builder()
        .registerMetricReader(
            PeriodicMetricReader.builder(metricExporter)
                .setInterval(Duration.ofSeconds(15))
                .build())
        .build();

    return OpenTelemetrySdk.builder()
        .setMeterProvider(meterProvider)
        .build();
  }
}
