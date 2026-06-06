package com.aditya.distributedratelimiter.interceptor;

import com.aditya.distributedratelimiter.config.RateLimitProperties;
import com.aditya.distributedratelimiter.constants.HeaderConstants;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
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
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private RateLimiterInterceptor rateLimiterInterceptor;

  private StringWriter responseBody;
  private PrintWriter responseWriter;

  @BeforeEach
  void setUp() {
    RateLimitProperties properties = new RateLimitProperties(
        5,
        Duration.ofSeconds(60),
        RateLimitProperties.Strategy.FIXED_WINDOW
    );
    rateLimiterInterceptor = new RateLimiterInterceptor(rateLimiterService, properties);

    responseBody = new StringWriter();
    responseWriter = new PrintWriter(responseBody);
  }

  @Test
  void shouldRejectWhenUserIdHeaderMissing() throws Exception  {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn(null);
    when(response.getWriter()).thenReturn(responseWriter);

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());
    responseWriter.flush(); // Ensure all output is written to the StringWriter

    // Assert
    assertFalse(result);
    verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldRejectWhenHeaderIdBlank() throws Exception {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn("   ");
    when(response.getWriter()).thenReturn(responseWriter);

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());
    responseWriter.flush(); // Ensure all output is written to the StringWriter

    // Assert
    assertFalse(result);
    verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldAllowRequest() throws Exception {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn("usera");
    when(rateLimiterService.validateRequest("usera"))
        .thenReturn(new RateLimitResult(true, 4, 0));

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());

    // Assert
    assertTrue(result);
    verify(response, never()).setStatus(anyInt());
    verify(rateLimiterService).validateRequest("usera");
  }

  @Test
  void shouldRejectWhenRateLimitExceeded() throws Exception {
    // Arrange
    when(request.getHeader(HeaderConstants.USER_ID_HEADER)).thenReturn("userb");
    when(rateLimiterService.validateRequest("userb"))
        .thenReturn(new RateLimitResult(false, 0, 30));
    when(response.getWriter()).thenReturn(responseWriter);

    // Act
    boolean result = rateLimiterInterceptor.preHandle(request, response, new Object());
    responseWriter.flush(); // Ensure all output is written to the StringWriter

    // Assert
    assertFalse(result);
    verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    verify(response).setHeader("Retry-After", "30");
  }
}
