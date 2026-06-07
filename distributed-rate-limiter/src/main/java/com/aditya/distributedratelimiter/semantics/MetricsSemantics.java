package com.aditya.distributedratelimiter.semantics;

import io.opentelemetry.api.common.AttributeKey;


public final class MetricsSemantics {

  private MetricsSemantics() {
    // Prevent instantiation
  }

  public enum Metrics {
    TOTAL_REQUESTS("ratelimiter.requests.total", "{requests}", "Total number of requests received"),

    REDIS_LATENCY("ratelimiter.redis.latency", "ms", "Latency of Redis operations in milliseconds");

    private final String name;
    private final String unit;
    private final String description;

    Metrics(String name, String unit, String description) {
      this.name = name;
      this.unit = unit;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public String getUnit() {
      return unit;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum Attributes {
    STATUS(AttributeKey.stringKey("status"), "Status of the request (allowed/rejected)"),
    STRATEGY(AttributeKey.stringKey("strategy"), "Rate limiting strategy used to evaluate the request");

    private final AttributeKey<String> _attributeKey;
    private final String _description;

    Attributes(AttributeKey<String> key, String description) {
      this._attributeKey = key;
      this._description = description;
    }

    public AttributeKey<String> getAttributeKey() {
      return this._attributeKey;
    }

    public String getDescription() {
      return this._description;
    }
  }

  public static final class STATUS {
    public static final String ALLOWED = "allowed";
    public static final String REJECTED = "rejected";
  }

  public static final class STRATEGY {
    public static final String FIXED_WINDOW = "fixed_window";
    public static final String SLIDING_WINDOW = "sliding_window";
  }

}
