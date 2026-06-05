package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.store.RedisRateLimitStore;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "rate.limiter.strategy", havingValue = "redis-fixed-window")
public class RedisFixedWindowStrategy implements RateLimitingStrategy {

  private static final Logger _log = LoggerFactory.getLogger(RedisFixedWindowStrategy.class);


  private final int maxRequests;
  private final long windowSize;

  private final RedisRateLimitStore redisRateLimitStore;

  public RedisFixedWindowStrategy(
        RedisRateLimitStore redisRateLimitStore,
      @Value("${rate.limit.max-requests}") int maxRequests,
      @Value("${rate.limit.window-size-sec}") long windowSizeSeconds) {
    this.redisRateLimitStore = redisRateLimitStore;
    this.maxRequests = maxRequests; // Max requests per window
    this.windowSize = windowSizeSeconds; // Window size in seconds
  }

  @Override
  public RateLimitResult validate(String userId) {
    _log.info("Validating request for user: {}", userId);
    long count = redisRateLimitStore.incrementAndExpire(userId, Duration.ofSeconds(windowSize));
    boolean allowed = count <= maxRequests;
    long remaining = Math.max(0, maxRequests - count);

    return new RateLimitResult(allowed, (int) remaining, windowSize);
  }

  @Override
  public void clear() {
    _log.info("Clearing Redis rate limit store");
     redisRateLimitStore.clear();
  }
}


