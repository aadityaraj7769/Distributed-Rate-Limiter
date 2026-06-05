package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.model.UserRequestData;
import com.aditya.distributedratelimiter.service.RateLimiterService;
import com.aditya.distributedratelimiter.store.RateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "rate.limiter.strategy", havingValue = "fixed-window", matchIfMissing = true)
public class FixedWindowStrategy implements RateLimitingStrategy{

  private static final Logger _log = LoggerFactory.getLogger(RateLimiterService.class);

  private final int maxRequests ; // Max requests per window

  private final long windowSize; // 1 minute in milliseconds

  private final RateLimitStore rateLimitStore;

    public FixedWindowStrategy(
        RateLimitStore rateLimitStore,
        @Value("${rate.limit.max-requests}") int maxRequests,
        @Value("${rate.limit.window-size-sec}") long windowSizeSeconds) {
      this.windowSize = windowSizeSeconds * 1000;
      this.maxRequests = maxRequests;
      this.rateLimitStore = rateLimitStore;
    }

  public  int getMaxRequests() {
    return maxRequests;
  }

    @Override
    public synchronized RateLimitResult validate(String userId) {
      long currentTime = System.currentTimeMillis();
      UserRequestData data = rateLimitStore.getUserRequestData(userId);

      if(data == null) {
        _log.info("First request for user: {}", userId);
        rateLimitStore.save(userId, new UserRequestData(1, currentTime));
        return new RateLimitResult(true, maxRequests - 1, 0);
      }

      // Window expired
      if (currentTime - data.getWindowStart() >= windowSize) {
        _log.info("New window started for user: {}", userId);
        data.setRequestCount(1);
        data.setWindowStart(currentTime);
        rateLimitStore.save(userId, data);
        return new RateLimitResult(true, maxRequests - 1, 0);
      }

      // Window active
      data.setRequestCount(data.getRequestCount() + 1);
      rateLimitStore.save(userId, data);
      return new RateLimitResult(
          data.getRequestCount() <= maxRequests,
          Math.max(0, maxRequests - data.getRequestCount()),
          (windowSize - (currentTime - data.getWindowStart())) / 1000
      );
    }

    @Override
    public void clear() {
      rateLimitStore.clear();
    }
}
