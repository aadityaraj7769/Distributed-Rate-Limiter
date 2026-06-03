package com.aditya.distributedratelimiter;

import com.aditya.distributedratelimiter.store.RateLimitStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class RateLimiterIntegrationTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RateLimitStore rateLimitStore;

  @BeforeEach
  void setUp() {
    rateLimitStore.clear();
  }

  @Test
  void shouldReturnPong() throws Exception {
    mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
        .andExpect(status().isOk())
        .andExpect(content().string("pong"));
  }

  @Test
  void shouldRejectRequestWhenHeaderMissing() throws Exception {
    mockMvc.perform(get("/ping"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn429WhenLimitExceeded() throws Exception {
    // Simulate 5 requests to reach the limit
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
          .andExpect(status().isOk());
    }

    // 6th request should be rejected
    mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
        .andExpect(status().isTooManyRequests());
  }

  @Test
  void shouldReturnRetryAfterHeader() throws Exception {
    // Simulate 5 requests to reach the limit
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
          .andExpect(status().isOk());
    }

    // 6th request should be rejected with Retry-After header
    mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void shouldReturnRateLimitHeaders() throws Exception {
    // Simulate 5 requests to reach the limit
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
          .andExpect(status().isOk());
    }

    // 6th request should be rejected with rate limit headers
    mockMvc.perform(get("/ping").header("X-User-Id", "user1"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("X-RateLimit-Limit", "5"))
        .andExpect(header().string("X-RateLimit-Remaining", "0"));
  }

}
