package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.store.RedisSlidingWindowRateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "rate.limiter.strategy", havingValue = "redis-sliding-window")
public class SlidingWindowStrategy implements RateLimitingStrategy {

  Logger _log = LoggerFactory.getLogger(SlidingWindowStrategy.class);

  private final int maxRequests; // Max requests per window
  private final long windowSize; // Window size in seconds
  private final RedisSlidingWindowRateLimitStore redisStore;

  public SlidingWindowStrategy(
      RedisSlidingWindowRateLimitStore redisStore,
      @Value("${rate.limit.max-requests}") int maxRequests,
      @Value("${rate.limit.window-size-sec}") long windowSizeSeconds) {
    this.redisStore = redisStore;
    this.maxRequests = maxRequests;
    this.windowSize = windowSizeSeconds;
  }

  @Override
  public RateLimitResult validate(String userId) {
    _log.info("Validating request for user: {}", userId);
    long now = System.currentTimeMillis();
    long windowSizeMs = windowSize * 1000L; // Convert seconds to milliseconds

    long count = redisStore.checkAndAdd(userId, windowSize, maxRequests);

    if (count == RedisSlidingWindowRateLimitStore.DENIED) {
      Long oldest = redisStore.getOldestTimestamp(userId);
      long retryAfterSeconds = windowSize;
      if(oldest != null) {
        long retryAfterMs = (oldest + windowSizeMs) - now;
        retryAfterSeconds = Math.max(1, (long) Math.ceil(retryAfterMs / 1000.0)); // Convert ms to seconds, round up
      }

      _log.info("User: {} DENIED. Count: {}, retryAfter: {}s", userId, count, retryAfterSeconds);
      return new RateLimitResult(false, 0, retryAfterSeconds);
    }

    long remaining = Math.max(0, maxRequests - count);
    _log.info("User: {} ALLOWED, Count: {}, Remaining: {}", userId, count, remaining);
    return new RateLimitResult(true, (int) remaining, 0);
  }

  @Override
  public void clear() {
    redisStore.clear();
  }
}
