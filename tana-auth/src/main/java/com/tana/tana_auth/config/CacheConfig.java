package com.tana.tana_auth.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import com.tana.tana_auth.functions.places.dto.PlacesListResponseDto;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000));

        // A place-list entry can contain the entire island, so bound retained rows,
        // not just the number of per-user/search keys.
        manager.registerCustomCache("place-list", Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumWeight(10_000)
            .weigher((Object key, Object value) -> value instanceof PlacesListResponseDto places
                && places.getPlaceList() != null ? Math.max(1, places.getPlaceList().size()) : 1)
            .build());
        for (String name : new String[] { "collections-list-response", "home-v2-response",
                "collectionDetails", "saved-list", "events-list" }) {
            manager.registerCustomCache(name, Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(128).build());
        }
        manager.registerCustomCache("spot-images", Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumWeight(8 * 1024 * 1024)
            .weigher((Object key, Object value) -> {
                long weight = 128L + key.toString().length() * 2L;
                if (value instanceof java.util.List<?> images) {
                    for (Object image : images) weight += 64L + image.toString().length() * 2L;
                }
                return (int) Math.min(Integer.MAX_VALUE, weight);
            }).build());

        return manager;
    }
}
