package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.config.RateLimitProperties;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.model.UserRequestData;
import com.aditya.distributedratelimiter.semantics.MetricsSemantics;
import com.aditya.distributedratelimiter.store.RateLimitStore;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "rate-limit.strategy", havingValue = "fixed-window", matchIfMissing = true)
public class FixedWindowStrategy implements RateLimitingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(FixedWindowStrategy.class);

  private final RateLimitProperties properties;
  private final RateLimitStore rateLimitStore;
  private final Clock clock;

  public FixedWindowStrategy(
      RateLimitStore rateLimitStore,
      RateLimitProperties properties,
      Clock clock) {
    this.rateLimitStore = rateLimitStore;
    this.properties = properties;
    this.clock = clock;
  }

  public int getMaxRequests() {
    return properties.maxRequests();
  }

  @Override
  public synchronized RateLimitResult validate(String userId) {
    long currentTime = clock.millis();
    long windowSizeMs = properties.windowSizeMillis();
    int maxRequests = properties.maxRequests();
    UserRequestData data = rateLimitStore.getUserRequestData(userId);

    if (data == null) {
      LOG.debug("First request for user: {}", userId);
      rateLimitStore.save(userId, new UserRequestData(1, currentTime));
      return new RateLimitResult(true, maxRequests - 1, 0);
    }

    // Window expired, reset request count
    if (currentTime - data.getWindowStart() >= windowSizeMs) {
      LOG.debug("New window started for user: {}", userId);
      data.setRequestCount(1);
      data.setWindowStart(currentTime);
      rateLimitStore.save(userId, data);
      return new RateLimitResult(true, maxRequests - 1, 0);
    }

    // Window active, increment request count
    data.setRequestCount(data.getRequestCount() + 1);
    rateLimitStore.save(userId, data);
    return new RateLimitResult(
        data.getRequestCount() <= maxRequests,
        Math.max(0, maxRequests - data.getRequestCount()),
        (windowSizeMs - (currentTime - data.getWindowStart())) / 1000
    );
  }

  @Override
  public void clear() {
    rateLimitStore.clear();
  }

  @Override
  public String getStrategyName() {
    return MetricsSemantics.STRATEGY.FIXED_WINDOW;
  }
}
