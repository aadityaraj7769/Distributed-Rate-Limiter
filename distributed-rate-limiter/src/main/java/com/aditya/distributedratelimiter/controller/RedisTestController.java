package com.aditya.distributedratelimiter.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/redis")
public class RedisTestController {

  private final RedisTemplate<String, String> redisTemplate;

  public RedisTestController(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @GetMapping
  public String testRedis() {
    redisTemplate.opsForValue().set("Hello", "World");
    return redisTemplate.opsForValue().get("Hello");
  }
}
