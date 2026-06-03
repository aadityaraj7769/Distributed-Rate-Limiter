package com.aditya.distributedratelimiter.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;


public class RateLimitResult {

  private boolean allowed;
  private int remainingRequests;
  private long retryAfterSeconds;

  private final ObjectMapper mapper = new ObjectMapper();

  public RateLimitResult(boolean allowed, int remainingRequests, long retryAfterSeconds) {
    this.allowed = allowed;
    this.remainingRequests = remainingRequests;
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public String toTooManyRequestsJson() throws JsonProcessingException {
    return mapper.writeValueAsString(Map.of(
        "error", "Too Many Requests",
        "retryAfterSeconds", retryAfterSeconds
    ));
  }

  public boolean isAllowed() {
    return allowed;
  }

  public int getRemainingRequests() {
    return remainingRequests;
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
