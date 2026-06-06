package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.config.RateLimitProperties;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.store.CheckResult;
import com.aditya.distributedratelimiter.store.RedisSlidingWindowRateLimitStore;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "rate-limit.strategy", havingValue = "redis-sliding-window")
public class SlidingWindowStrategy implements RateLimitingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(SlidingWindowStrategy.class);

  private final RateLimitProperties properties;
  private final RedisSlidingWindowRateLimitStore redisStore;
  private final Clock clock;

  public SlidingWindowStrategy(
      RedisSlidingWindowRateLimitStore redisStore,
      RateLimitProperties properties,
      Clock clock) {
    this.redisStore = redisStore;
    this.properties = properties;
    this.clock = clock;
  }

  @Override
  public RateLimitResult validate(String userId) {
    LOG.debug("Validating request for user: {}", userId);
    long now = clock.millis();
    long windowSizeMs = properties.windowSizeMillis();
    int maxRequests = properties.maxRequests();

    CheckResult result = redisStore.checkAndAdd(userId, now, properties.windowSizeSeconds(), maxRequests);

    if (!result.allowed()) {
      long retryAfterSeconds = computeRetryAfterSeconds(result, now, windowSizeMs);
      LOG.info("User: {} DENIED. count: {}, retryAfter: {}s",
          userId, result.count(), retryAfterSeconds);
      return new RateLimitResult(false, 0, retryAfterSeconds);
    }

    long remaining = Math.max(0, maxRequests - result.count());
    LOG.debug("User: {} ALLOWED, Count: {}, Remaining: {}",
        userId, result.count(), remaining);
    return new RateLimitResult(true, (int) remaining, 0);
  }

  private long computeRetryAfterSeconds(CheckResult result, long now, long windowSizeMs) {
    return result.oldestTimestamp()
        .map(oldest -> Math.max(1L, (long) Math.ceil(((oldest + windowSizeMs) - now) / 1000.0)))
        .orElseGet(properties::windowSizeSeconds);
  }

  @Override
  public void clear() {
    redisStore.clear();
  }
}
