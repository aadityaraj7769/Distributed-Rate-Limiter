package com.aditya.distributedratelimiter.store;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;


@Component
public class RedisSlidingWindowRateLimitStore {

  private static final String KEY_PREFIX = "rate_limit:";
  private static final long NO_OLDEST = -1L;

  private final StringRedisTemplate redisTemplate;
  @SuppressWarnings("rawtypes")
  private final RedisScript<List> slidingWindowScript;

  public RedisSlidingWindowRateLimitStore(
      StringRedisTemplate redisTemplate,
      @SuppressWarnings("rawtypes") RedisScript<List> slidingWindowScript) {
    this.redisTemplate = redisTemplate;
    this.slidingWindowScript = slidingWindowScript;
  }

  /**
   * Atomically checks the sliding-window count and adds the current timestamp if allowed.
   * The entire operation runs as a single Lua script inside Redis, eliminating the
   * check-then-act race that exists with separate ZCARD/ZADD calls.
   */
  public CheckResult checkAndAdd(String userId, long nowMs, long windowSize, int maxRequests) {
    String key = KEY_PREFIX + userId;
    long windowSizeMs = windowSize * 1000L;
    String member = nowMs + ":" + UUID.randomUUID();

    @SuppressWarnings("unchecked")
    List<Long> result = redisTemplate.execute(
        slidingWindowScript,
        Collections.singletonList(key),
        Long.toString(nowMs),
        Long.toString(windowSizeMs),
        Integer.toString(maxRequests),
        Long.toString(windowSize),
        member);

    long allowed = result.get(0);
    long count = result.get(1);
    long oldest = result.get(2);
    Long oldestTimestamp = oldest == NO_OLDEST ? null : oldest;

    if (allowed == 1L) {
      return CheckResult.allowed(count, oldestTimestamp);
    }
    return CheckResult.denied(count, oldestTimestamp == null ? nowMs : oldestTimestamp);
  }

  public void clear() {
    Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }
}
