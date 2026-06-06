package com.aditya.distributedratelimiter.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
    int maxRequests,
    Duration windowSize,
    Strategy strategy
) {

  public enum Strategy {
    FIXED_WINDOW,
    REDIS_FIXED_WINDOW,
    REDIS_SLIDING_WINDOW
  }

  public long windowSizeSeconds() {
    return windowSize.toSeconds();
  }

  public long windowSizeMillis() {
    return windowSize.toMillis();
  }
}
