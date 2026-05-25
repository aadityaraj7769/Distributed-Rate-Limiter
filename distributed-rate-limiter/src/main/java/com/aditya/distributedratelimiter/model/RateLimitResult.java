package com.aditya.distributedratelimiter.model;

public class RateLimitResult {

  private boolean allowed;
  private int remainingRequests;
  private long retryAfterSeconds;

  public static final String TOO_MANY_REQUESTS_JSON = """
      {
          "error": "Too Many Requests"
      }
      """;

  public RateLimitResult(boolean allowed, int remainingRequests, long retryAfterSeconds) {
    this.allowed = allowed;
    this.remainingRequests = remainingRequests;
    this.retryAfterSeconds = retryAfterSeconds;
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
