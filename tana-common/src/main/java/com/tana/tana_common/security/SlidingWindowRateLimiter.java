package com.tana.tana_common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import java.util.ArrayDeque;

/** Bounded per-client sliding windows; idle clients are evicted automatically. */
final class SlidingWindowRateLimiter {
    private final Cache<String, ArrayDeque<Long>> clients;
    private final Ticker ticker;
    private final int limit;
    private final long windowNanos;

    SlidingWindowRateLimiter(int limit, Duration window, long maxClients, Ticker ticker) {
        this.limit = limit;
        this.ticker = ticker;
        this.windowNanos = window.toNanos();
        this.clients = Caffeine.newBuilder().maximumSize(maxClients)
            .expireAfterAccess(window).ticker(ticker).build();
    }

    boolean tryAcquire(String client) {
        ArrayDeque<Long> timestamps = clients.get(client, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            long now = ticker.read();
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= limit) return false;
            timestamps.addLast(now);
            return true;
        }
    }

    long retainedClients() {
        clients.cleanUp();
        return clients.estimatedSize();
    }
}
