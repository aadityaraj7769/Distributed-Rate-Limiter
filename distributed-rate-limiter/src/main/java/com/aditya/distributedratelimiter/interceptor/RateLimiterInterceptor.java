package com.aditya.distributedratelimiter.interceptor;

import com.aditya.distributedratelimiter.constants.HeaderConstants;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.service.RateLimiterService;
import com.aditya.distributedratelimiter.strategy.RateLimitingStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

  private static final Logger _log = LoggerFactory.getLogger(RateLimiterInterceptor.class);

  @Value("${rate.limit.max-requests}")
  private int maxRequests ;

  private final RateLimiterService rateLimiterService;
  private final RateLimitingStrategy rateLimitingStrategy;


  public RateLimiterInterceptor(RateLimiterService rateLimiterService, RateLimitingStrategy rateLimitingStrategy) {
    this.rateLimiterService = rateLimiterService;
    this.rateLimitingStrategy = rateLimitingStrategy;
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
          String.valueOf(maxRequests)
      );

      response.setHeader(
          "X-RateLimit-Remaining",
          String.valueOf(result.getRemainingRequests())
      );
      response.getWriter().write(result.toTooManyRequestsJson());
      return false;
    }

    return true;

  }
}
