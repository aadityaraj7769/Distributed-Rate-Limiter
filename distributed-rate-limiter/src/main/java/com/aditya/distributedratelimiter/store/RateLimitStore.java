package com.aditya.distributedratelimiter.store;

import com.aditya.distributedratelimiter.model.UserRequestData;


public interface RateLimitStore {
  UserRequestData getUserRequestData(String userId);

  void save(String userId, UserRequestData data);

  void clear();
}
