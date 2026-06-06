package com.aditya.distributedratelimiter.store;

import java.util.Optional;

/**
 * Outcome of a sliding-window check-and-add operation.
 */
public record CheckResult(boolean allowed, long count, Long oldestTimestampMs) {

  public static CheckResult denied(long count, long oldestTimestampMs) {
    return new CheckResult(false, count, oldestTimestampMs);
  }

  public static CheckResult allowed(long count, Long oldestTimestampMs) {
    return new CheckResult(true, count, oldestTimestampMs);
  }

  public Optional<Long> oldestTimestamp() {
    return Optional.ofNullable(oldestTimestampMs);
  }
}
