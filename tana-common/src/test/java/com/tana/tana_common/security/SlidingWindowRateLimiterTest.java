package com.tana.tana_common.security;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowRateLimiterTest {
    @Test void expiresIdleClientsAndReopensTheWindow() {
        AtomicLong now = new AtomicLong();
        var limiter = new SlidingWindowRateLimiter(2, Duration.ofMinutes(1), 10, now::get);
        assertTrue(limiter.tryAcquire("client"));
        assertTrue(limiter.tryAcquire("client"));
        assertFalse(limiter.tryAcquire("client"));
        now.set(Duration.ofMinutes(1).toNanos());
        assertEquals(0, limiter.retainedClients());
        assertTrue(limiter.tryAcquire("client"));
    }

    @Test void retainsOnlyBoundedClientState() {
        var limiter = new SlidingWindowRateLimiter(100, Duration.ofMinutes(1), 10, () -> 0L);
        for (int i = 0; i < 1000; i++) limiter.tryAcquire("client-" + i);
        assertTrue(limiter.retainedClients() <= 10);
    }

    @Test void concurrentRequestsCannotExceedTheLimit() throws Exception {
        var limiter = new SlidingWindowRateLimiter(100, Duration.ofMinutes(1), 10, () -> 0L);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var calls = new ArrayList<Callable<Boolean>>();
            for (int i = 0; i < 300; i++) calls.add(() -> limiter.tryAcquire("same-client"));
            int accepted = 0;
            for (Future<Boolean> result : pool.invokeAll(calls)) if (result.get()) accepted++;
            assertEquals(100, accepted);
        } finally {
            pool.shutdownNow();
        }
    }
}
