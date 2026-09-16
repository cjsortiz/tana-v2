package com.tana.tana_auth.config;

import com.tana.tana_auth.functions.places.dto.PlacesListResponseDto;
import com.tana.tana_auth.functions.places.dto.PlacesDetailsResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCache;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {
    @Test void boundsTheNumberOfRetainedPlaceRows() {
        var manager = new CacheConfig().cacheManager();
        var cache = (CaffeineCache) manager.getCache("place-list");
        var response = PlacesListResponseDto.builder().placeList(Collections.nCopies(1000,
            PlacesDetailsResponseDto.builder().id(1L).build())).build();
        for (int i = 0; i < 30; i++) cache.put("user-" + i, response);
        cache.getNativeCache().cleanUp();
        assertTrue(cache.getNativeCache().policy().eviction().orElseThrow().weightedSize().orElseThrow() <= 10_000);
        assertTrue(cache.getNativeCache().estimatedSize() <= 10);
    }

    @Test void largeResponsesAndImageListsHaveExplicitBudgets() {
        var manager = new CacheConfig().cacheManager();
        var home = (CaffeineCache) manager.getCache("home-v2-response");
        var images = (CaffeineCache) manager.getCache("spot-images");
        assertEquals(128, home.getNativeCache().policy().eviction().orElseThrow().getMaximum());
        assertEquals(8 * 1024 * 1024, images.getNativeCache().policy().eviction().orElseThrow().getMaximum());
    }
}
