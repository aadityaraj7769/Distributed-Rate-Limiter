package com.aditya.distributedratelimiter.strategy;

import com.aditya.distributedratelimiter.model.RateLimitResult;


public interface RateLimitingStrategy {
  RateLimitResult validate(String userId);
}
