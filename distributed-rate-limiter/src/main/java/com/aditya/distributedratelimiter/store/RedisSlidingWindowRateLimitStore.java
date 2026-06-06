package com.aditya.distributedratelimiter.store;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;


@Component
public class RedisSlidingWindowRateLimitStore {

  private static final String KEY_PREFIX = "rate_limit:";

  private final StringRedisTemplate redisTemplate;

  public RedisSlidingWindowRateLimitStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Checks the sliding-window count and adds the current timestamp if the request is allowed.
   */
  public CheckResult checkAndAdd(String userId, long nowMs, long windowSize, int maxRequests) {
    String key = KEY_PREFIX + userId;
    long windowStart = nowMs - windowSize * 1000L;

    redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

    Long current = redisTemplate.opsForZSet().zCard(key);
    long count = current == null ? 0L : current;

    if (count >= maxRequests) {
      return CheckResult.denied(count, oldestTimestamp(key));
    }

    String member = nowMs + ":" + UUID.randomUUID();
    redisTemplate.opsForZSet().add(key, member, nowMs);
    redisTemplate.expire(key, windowSize, TimeUnit.SECONDS);

    return CheckResult.allowed(count + 1, oldestTimestamp(key));
  }

  public void clear() {
    Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  private Long oldestTimestamp(String key) {
    Set<ZSetOperations.TypedTuple<String>> oldest =
        redisTemplate.opsForZSet().rangeWithScores(key, 0, 0);
    if (oldest == null || oldest.isEmpty()) {
      return null;
    }
    Double score = oldest.iterator().next().getScore();
    return score == null ? null : score.longValue();
  }
}
