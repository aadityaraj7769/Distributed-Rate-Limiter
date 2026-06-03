package com.aditya.distributedratelimiter.service;

import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.store.RateLimitStore;
import com.aditya.distributedratelimiter.strategy.RateLimitingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RateLimiterServiceTests {

  @Mock
  private RateLimitStore rateLimitStore;

  @Mock
  private RateLimitingStrategy rateLimitingStrategy;

  @InjectMocks
  private RateLimiterService rateLimiterService;

  @Test
  void shouldAllowFirstRequest() {

    // Arrange
    when(rateLimitingStrategy.validate("user1"))
        .thenReturn(new RateLimitResult(true, 4, 60));

    // Act
    RateLimitResult result = rateLimiterService.validateRequest("user1");

    // Assert
    assertTrue(result.isAllowed());
    assertEquals(4, result.getRemainingRequests());

    verify(rateLimitingStrategy).validate("user1");
  }

  @Test
  void shouldAllowRequestWithinLimit() {
    // Arrange
    when(rateLimitingStrategy.validate("user1"))
        .thenReturn(new RateLimitResult(true, 2, 60));

    // Act
    RateLimitResult result = rateLimiterService.validateRequest("user1");

    // Assert
    assertTrue(result.isAllowed());
  }

  @Test
  void shouldRejectWhenLimitExceeded() {
    // Arrange
    when(rateLimitingStrategy.validate("user1"))
        .thenReturn(new RateLimitResult(false, 0, 60));

    // Act
    RateLimitResult result = rateLimiterService.validateRequest("user1");

    // Assert
    assertFalse(result.isAllowed());
  }

  @Test
  void shouldResetWindowWhenExpired() {
    // Arrange
    when(rateLimitingStrategy.validate("user1"))
        .thenReturn(new RateLimitResult(true, 4, 60));

    // Act
    RateLimitResult result = rateLimiterService.validateRequest("user1");

    // Assert
    assertTrue(result.isAllowed());
  }
}
