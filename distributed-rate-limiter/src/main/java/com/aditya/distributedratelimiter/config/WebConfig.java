package com.aditya.distributedratelimiter.config;

import com.aditya.distributedratelimiter.interceptor.RateLimiterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private RateLimiterInterceptor rateLimiterInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimiterInterceptor)
        .excludePathPatterns("/ping");
  }
}
