package com.aditya.distributedratelimiter.interceptor;

import com.aditya.distributedratelimiter.constants.HeaderConstants;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

  private final RateLimiterService rateLimiterService;
  private static final Logger _log = LoggerFactory.getLogger(RateLimiterInterceptor.class);

  public RateLimiterInterceptor(RateLimiterService rateLimiterService) {
    this.rateLimiterService = rateLimiterService;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws Exception {

    String userId = request.getHeader(HeaderConstants.USER_ID_HEADER);

    if(userId == null || userId.isBlank()) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.getWriter().write("Missing X-User-Id header");
      return false;
    }

    RateLimitResult result = rateLimiterService.validateRequest(userId);

    if(!result.isAllowed()) {
      _log.warn("Rate limit exceeded for user: {}", userId);
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(HeaderConstants.APPLICATION_JSON);
      response.setHeader(
          "Retry-After",
          String.valueOf(result.getRetryAfterSeconds())
      );
      response.setHeader(
          "X-RateLimit-Limit",
          String.valueOf(rateLimiterService.getMaxRequests())
      );

      response.setHeader(
          "X-RateLimit-Remaining",
          String.valueOf(result.getRemainingRequests())
      );
      response.getWriter().write(RateLimitResult.TOO_MANY_REQUESTS_JSON);
      return false;
    }

    return true;

  }
}
