package com.aditya.distributedratelimiter.store;

import com.aditya.distributedratelimiter.service.MetricsService;
import java.time.Duration;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimitStore {

  private static final String KEY_PREFIX = "rate_limit:";

  private final StringRedisTemplate redisTemplate;
  private final MetricsService metricsService;

  public RedisRateLimitStore(StringRedisTemplate redisTemplate, MetricsService metricsService) {
    this.redisTemplate = redisTemplate;
    this.metricsService = metricsService;
  }

  public long incrementAndExpire(String userId, Duration ttl) {
    String key = KEY_PREFIX + userId;
    long startNanos = System.nanoTime();
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redisTemplate.expire(key, ttl);
    }
    double durationMs = (System.nanoTime() - startNanos) / 1_000_000.0;
    metricsService.recordRedisLatency(durationMs);
    return count != null ? count : 0;
  }

  public void clear() {
    Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }
}
