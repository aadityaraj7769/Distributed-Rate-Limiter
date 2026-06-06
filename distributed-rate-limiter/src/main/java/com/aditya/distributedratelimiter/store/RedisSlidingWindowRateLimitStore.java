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

  public long checkAndAdd(String userId, long windowSize, int maxRequests) {
    String key = KEY_PREFIX + userId;
    long now = System.currentTimeMillis();
    long windowStart = now - windowSize * 1000L;

    redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

    Long current = redisTemplate.opsForZSet().zCard(key);
    long count = current == null ? 0L : current;

    if (count >= maxRequests) {
      return DENIED;
    }

    String member = now + ":" + UUID.randomUUID();
    redisTemplate.opsForZSet().add(key, member, now);
    redisTemplate.expire(key, windowSize, TimeUnit.SECONDS);

    return count + 1;
  }

  public static final long DENIED = -1L;

  public Long getOldestTimestamp(String userId) {
    String key = KEY_PREFIX + userId;
    Set<ZSetOperations.TypedTuple<String>> oldest =
        redisTemplate.opsForZSet().rangeWithScores(key, 0, 0);
    if (oldest == null || oldest.isEmpty()) {
      return null;
    }

    Double score = oldest.iterator().next().getScore();
    return score == null ? null : score.longValue();
  }

  public void clear() {
    redisTemplate.delete(redisTemplate.keys(KEY_PREFIX + "*"));
  }
}
