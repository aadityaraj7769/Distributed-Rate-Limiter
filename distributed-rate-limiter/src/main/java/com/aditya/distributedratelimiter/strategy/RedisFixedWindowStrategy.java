package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.config.RateLimitProperties;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.store.RedisRateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "rate-limit.strategy", havingValue = "redis-fixed-window")
public class RedisFixedWindowStrategy implements RateLimitingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(RedisFixedWindowStrategy.class);

  private final RateLimitProperties properties;
  private final RedisRateLimitStore redisRateLimitStore;

  public RedisFixedWindowStrategy(
      RedisRateLimitStore redisRateLimitStore,
      RateLimitProperties properties) {
    this.redisRateLimitStore = redisRateLimitStore;
    this.properties = properties;
  }

  @Override
  public RateLimitResult validate(String userId) {
    LOG.debug("Validating request for user: {}", userId);
    long count = redisRateLimitStore.incrementAndExpire(userId, properties.windowSize());
    int maxRequests = properties.maxRequests();
    boolean allowed = count <= maxRequests;
    long remaining = Math.max(0, maxRequests - count);

    LOG.debug("User: {}, Count: {}, Allowed: {}, Remaining: {}", userId, count, allowed, remaining);
    return new RateLimitResult(allowed, (int) remaining, properties.windowSizeSeconds());
  }

  @Override
  public void clear() {
    LOG.info("Clearing Redis rate limit store");
    redisRateLimitStore.clear();
  }
}
