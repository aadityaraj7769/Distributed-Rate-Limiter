package com.aditya.distributedratelimiter.interceptor;

import com.aditya.distributedratelimiter.config.RateLimitProperties;
import com.aditya.distributedratelimiter.constants.HeaderConstants;
import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.service.MetricsService;
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

  private static final Logger LOG = LoggerFactory.getLogger(RateLimiterInterceptor.class);

  private final RateLimiterService rateLimiterService;
  private final RateLimitProperties properties;
  private final MetricsService metricsService;

  public RateLimiterInterceptor(
      RateLimiterService rateLimiterService,
      RateLimitProperties properties,
      MetricsService metricsService
  ) {
    this.rateLimiterService = rateLimiterService;
    this.properties = properties;
    this.metricsService = metricsService;
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
    String strategyName = rateLimiterService.getStrategyName();

    if (!result.isAllowed()) {
      LOG.warn("Rate limit exceeded for user: {}", userId);
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(HeaderConstants.APPLICATION_JSON);
      response.setHeader(
          "Retry-After",
          String.valueOf(result.getRetryAfterSeconds())
      );
      response.setHeader(
          "X-RateLimit-Limit",
          String.valueOf(properties.maxRequests())
      );

      response.setHeader(
          "X-RateLimit-Remaining",
          String.valueOf(result.getRemainingRequests())
      );
      response.getWriter().write(result.toTooManyRequestsJson());
      metricsService.recordRejectedRequest(strategyName);
      return false;
    }

    metricsService.recordAllowedRequest(strategyName);
    return true;

  }
}
