package com.aditya.distributedratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "otel")
public class OtelProperties {

  /** OTLP gRPC endpoint of the OpenTelemetry Collector. */
  private String endpoint = "http://localhost:4317";

  /** Export interval in seconds for the PeriodicMetricReader. */
  private int exportIntervalSeconds = 15;

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public int getExportIntervalSeconds() {
    return exportIntervalSeconds;
  }

  public void setExportIntervalSeconds(int exportIntervalSeconds) {
    this.exportIntervalSeconds = exportIntervalSeconds;
  }
}
