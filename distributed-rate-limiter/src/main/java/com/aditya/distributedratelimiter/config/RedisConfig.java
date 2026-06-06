package com.aditya.distributedratelimiter.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;


@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  /**
   * Lua script for the atomic sliding-window check-and-add operation.
   * Loaded once at startup; subsequent calls use EVALSHA on the Redis side.
   */
  @Bean
  @SuppressWarnings("rawtypes")
  public RedisScript<List> slidingWindowScript() {
    DefaultRedisScript<List> script = new DefaultRedisScript<>();
    script.setLocation(new ClassPathResource("scripts/sliding_window.lua"));
    script.setResultType(List.class);
    return script;
  }
}
