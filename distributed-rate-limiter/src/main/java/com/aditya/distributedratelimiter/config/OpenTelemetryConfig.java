package com.aditya.distributedratelimiter.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenTelemetryConfig {

  // Port on which the OTel SDK exposes Prometheus-formatted metrics for scraping.
  // Prometheus must scrape this port (not the Spring Actuator port) to see OTel metrics.
  @Value("${otel.prometheus.port:9464}")
  private int prometheusPort;

  @Value("${otel.prometheus.host:0.0.0.0}")
  private String prometheusHost;

  // When false (e.g. in tests), no metric reader is registered — avoids binding the
  // Prometheus HTTP server port and prevents collisions across ApplicationContext reloads.
  @Value("${otel.prometheus.enabled:true}")
  private boolean prometheusEnabled;

  @Bean
  public OpenTelemetry opentelemetry() {
    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();

    if (prometheusEnabled) {
      meterProviderBuilder.registerMetricReader(
          PrometheusHttpServer.builder()
              .setHost(prometheusHost)
              .setPort(prometheusPort)
              .build());
    }

    return OpenTelemetrySdk.builder()
        .setMeterProvider(meterProviderBuilder.build())
        .build();
  }
}
