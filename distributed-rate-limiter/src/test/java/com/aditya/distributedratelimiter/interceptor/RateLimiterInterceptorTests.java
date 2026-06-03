package com.aditya.distributedratelimiter.interceptor;

import com.aditya.distributedratelimiter.constants.HeaderConstants;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.service.RateLimiterService;
import com.aditya.distributedratelimiter.strategy.RateLimitingStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RateLimiterInterceptorTests {
  @Mock
  private RateLimiterService rateLimiterService;

  @Mock
  private RateLimitingStrategy rateLimitingStrategy;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private RateLimiterInterceptor rateLimiterInterceptor;

  @BeforeEach
  void setUp() {
    rateLimiterInterceptor = new RateLimiterInterceptor(rateLimiterService, rateLimitingStrategy);
  }

  @Test
  void shouldRejectWhenUserIdHeaderMissing() throws Exception  {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn(null);

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

    // Assert
    assertFalse(result);
    verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldRejectWhenHeaderIdBlank() throws Exception {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn("   ");

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

    // Assert
    assertFalse(result);
    verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldAllowRequest() throws Exception {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn("user1");
    when(rateLimiterService.validateRequest("user1"))
        .thenReturn(new RateLimitResult(true, 4, 0));

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

    // Assert
    assertTrue(result);
    verify(response, never()).setStatus(anyInt());
    verify(rateLimiterService).validateRequest("user1");
  }

  @Test
  void shouldRejectWhenRateLimitExceeded() throws Exception {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn("user1");
    when(rateLimiterService.validateRequest("user1"))
        .thenReturn(new RateLimitResult(false, 0, 30));

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

    // Assert
    assertFalse(result);
    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    verify(response).setHeader("Retry-After", "30");
  }
}
