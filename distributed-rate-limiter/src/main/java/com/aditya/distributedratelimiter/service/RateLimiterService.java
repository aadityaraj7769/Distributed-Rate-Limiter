package com.aditya.distributedratelimiter.service;

import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.strategy.RateLimitingStrategy;
import org.springframework.stereotype.Service;


@Service
public class RateLimiterService {

  private final RateLimitingStrategy rateLimitingStrategy;

  public RateLimiterService(RateLimitingStrategy rateLimitingStrategy) {
    this.rateLimitingStrategy = rateLimitingStrategy;
  }

  public RateLimitResult validateRequest(String userId) {
    return rateLimitingStrategy.validate(userId);
  }

  public String getStrategyName() {
    return rateLimitingStrategy.getStrategyName();
  }

}
