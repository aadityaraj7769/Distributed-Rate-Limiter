package com.aditya.distributedratelimiter.model;

public class UserRequestData {
  private int requestCount;
  private long windowStart;

  public UserRequestData(int count, long start) {
    this.requestCount = count;
    this.windowStart = start;
  }

  public int getRequestCount() {
    return requestCount;
  }

  public void setRequestCount(int requestCount) {
    this.requestCount = requestCount;
  }

  public long getWindowStart() {
    return windowStart;
  }

  public void setWindowStart(long windowStart) {
    this.windowStart = windowStart;
  }
}
