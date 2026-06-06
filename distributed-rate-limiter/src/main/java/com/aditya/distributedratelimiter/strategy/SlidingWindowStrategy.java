package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.config.RateLimitProperties;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.store.RedisSlidingWindowRateLimitStore;
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

  public SlidingWindowStrategy(
      RedisSlidingWindowRateLimitStore redisStore,
      RateLimitProperties properties) {
    this.redisStore = redisStore;
    this.properties = properties;
  }

  @Override
  public RateLimitResult validate(String userId) {
    LOG.debug("Validating request for user: {}", userId);
    long now = System.currentTimeMillis();
    long windowSizeMs = properties.windowSizeMillis();
    long windowSizeSec = properties.windowSizeSeconds();
    int maxRequests = properties.maxRequests();

    long count = redisStore.checkAndAdd(userId, windowSizeSec, maxRequests);

    if (count == RedisSlidingWindowRateLimitStore.DENIED) {
      Long oldest = redisStore.getOldestTimestamp(userId);
      long retryAfterSeconds = windowSizeSec;
      if (oldest != null) {
        long retryAfterMs = (oldest + windowSizeMs) - now;
        retryAfterSeconds = Math.max(1, (long) Math.ceil(retryAfterMs / 1000.0));
      }

      LOG.info("User: {} DENIED. retryAfter: {}s", userId, retryAfterSeconds);
      return new RateLimitResult(false, 0, retryAfterSeconds);
    }

    long remaining = Math.max(0, maxRequests - count);
    LOG.debug("User: {} ALLOWED, Count: {}, Remaining: {}", userId, count, remaining);
    return new RateLimitResult(true, (int) remaining, 0);
  }

  @Override
  public void clear() {
    redisStore.clear();
  }
}
