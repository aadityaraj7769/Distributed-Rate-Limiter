package com.aditya.distributedratelimiter.service;

import com.aditya.distributedratelimiter.model.UserRequestData;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;


@Service
public class RateLimiterService {

  private final Map<String, UserRequestData> requestCounts = new HashMap<>();
  private static final int MAX_REQUESTS = 5; // Max requests per window
  private static final long WINDOW_SIZE = 60 * 1000; // 1 minute in milliseconds

  public boolean allowRequest(String userId) {
    long currentTime = System.currentTimeMillis();
    UserRequestData data = requestCounts.get(userId);

    if(data == null) {
      requestCounts.put(userId, new UserRequestData(1, currentTime));
      return true;
    }

    // Window expired
    if (currentTime - data.getWindowStart() >= WINDOW_SIZE) {
      data.setRequestCount(1);
      data.setWindowStart(currentTime);
      return true;
    }

    // Window active
    data.setRequestCount(data.getRequestCount() + 1);
    return data.getRequestCount() <= MAX_REQUESTS;
  }


}
