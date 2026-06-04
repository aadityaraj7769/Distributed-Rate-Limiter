package com.aditya.distributedratelimiter;

import com.aditya.distributedratelimiter.model.RateLimitResult;
import com.aditya.distributedratelimiter.service.RateLimiterService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class RateLimiterConcurrencyTest {
  @Autowired
  private RateLimiterService rateLimiterService;

  @RepeatedTest(100)
  void shouldNotAllowMoreThanLimitUnderConcurrentLoad()
      throws Exception {

    int threadCount = 20;

    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    CountDownLatch startLatch = new CountDownLatch(1);

    CountDownLatch finishLatch = new CountDownLatch(threadCount);

    List<Future<RateLimitResult>> futures = new ArrayList<>();
    String userId = UUID.randomUUID().toString();

    for (int i = 0; i < threadCount; i++) {

      futures.add(executor.submit(() -> {
        startLatch.await();
        try {
          return rateLimiterService.validateRequest(userId);
        } finally {
          finishLatch.countDown();
        }
      }));
    }

    startLatch.countDown();

    finishLatch.await();

    long allowed = futures.stream().map(f -> {
      try {
        return f.get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).filter(RateLimitResult::isAllowed).count();

    System.out.println("Allowed requests = " + allowed);

    assertTrue(
        allowed <= 5,
        "Allowed requests exceeded configured limit: " + allowed
    );

    executor.shutdown();
  }
}
