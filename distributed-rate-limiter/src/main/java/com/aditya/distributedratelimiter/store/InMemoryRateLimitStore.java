package com.aditya.distributedratelimiter.store;

import com.aditya.distributedratelimiter.model.UserRequestData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;


@Component
public class InMemoryRateLimitStore implements RateLimitStore {

  private final Map<String, UserRequestData> store = new ConcurrentHashMap<>();

  @Override
  public UserRequestData getUserRequestData(String userId) {
    return store.get(userId);
  }

  @Override
  public void save(String userId, UserRequestData data) {
    store.put(userId, data);
  }

  @Override
  public void clear() {
    store.clear();
  }
}
