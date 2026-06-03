package com.aditya.distributedratelimiter.config;

import com.aditya.distributedratelimiter.interceptor.RateLimiterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final RateLimiterInterceptor rateLimiterInterceptor;

  public WebConfig(RateLimiterInterceptor rateLimiterInterceptor) {
    this.rateLimiterInterceptor = rateLimiterInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimiterInterceptor);
      //  .excludePathPatterns("/ping");
  }
}
