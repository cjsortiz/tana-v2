package com.tana.tana_auth.functions.places;

import com.tana.tana_auth.functions.places.dto.PlacesRequestDto;
import com.tana.tana_auth.functions.places.repository.PlacesRepository;
import com.tana.tana_auth.functions.places.service.impl.PlacesServiceImpl;
import com.tana.tana_common.model.PlaceMaster;
import com.tana.tana_common.util.CommonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlaceCreationCacheTest {
    @Test
    void bothCreationPathsInvalidateUserFacingCachesOnlyAfterSuccess() throws Exception {
        String[] names = {"place-list", "collections", "collectionDetails",
            "collections-list-response", "home-v2-response"};
        ConcurrentMapCacheManager caches = new ConcurrentMapCacheManager(names);
        PlacesRepository repository = mock(PlacesRepository.class);
        when(repository.save(any(PlaceMaster.class))).thenAnswer(call -> {
            PlaceMaster place = call.getArgument(0);
            place.setId(42L);
            return place;
        });
        PlacesServiceImpl target = new PlacesServiceImpl();
        ReflectionTestUtils.setField(target, "repository", repository);
        CommonUtils commonUtils = mock(CommonUtils.class);
        when(commonUtils.uploadImage(anyString(), anyLong(), anyString(), any(), anyString()))
            .thenReturn("tana-place-images/blood-compact.jpg");
        ReflectionTestUtils.setField(target, "commonUtils", commonUtils);
        CacheInterceptor interceptor = new CacheInterceptor();
        interceptor.setCacheManager(caches);
        interceptor.setCacheOperationSources(new AnnotationCacheOperationSource());
        interceptor.afterPropertiesSet();
        interceptor.afterSingletonsInstantiated();
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(interceptor);
        PlacesServiceImpl service = (PlacesServiceImpl) factory.getProxy();
        PlacesRequestDto request = PlacesRequestDto.builder().name("Blood Compact").build();

        for (boolean withPhoto : new boolean[]{false, true}) {
            for (String name : names) caches.getCache(name).put("user", "old spots");
            if (withPhoto) {
                service.createPlaces(request, new MockMultipartFile("file", "place.jpg", "image/jpeg", new byte[]{1}));
            } else {
                service.createPlaces(request);
            }
            for (String name : names) assertNull(caches.getCache(name).get("user"), name);
        }

        caches.getCache("collectionDetails").put("user", "existing spots");
        when(repository.save(any(PlaceMaster.class))).thenThrow(new IllegalStateException("save failed"));
        assertThrows(IllegalStateException.class, () -> service.createPlaces(request));
        assertNotNull(caches.getCache("collectionDetails").get("user"));
    }
}
