package com.tana.tana_common.util;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommonUtilsImageCacheTest {
    @Test void batchUsesTheSameCacheAndOnlyRequestedPrefixes() {
        S3Client s3 = mock(S3Client.class);
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
            ListObjectsV2Response.builder().contents(S3Object.builder()
                .key("tana-place-images/Beach/photo.jpg").build()).build());
        CommonUtils utils = new CommonUtils();
        ReflectionTestUtils.setField(utils, "s3Client", s3);
        ReflectionTestUtils.setField(utils, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(utils, "cacheManager", new ConcurrentMapCacheManager("spot-images"));
        assertEquals(1, utils.getSpotImages("Beach").size());
        assertEquals(1, utils.getSpotImagesBySpotNames(List.of("Beach", "Beach")).get("Beach").size());
        verify(s3, times(1)).listObjectsV2(argThat((ListObjectsV2Request request) ->
            request.prefix().equals("tana-place-images/Beach/")));
    }

    @Test void followsPaginationInsideOnlyTheRequestedSpot() {
        S3Client s3 = mock(S3Client.class);
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
            ListObjectsV2Response.builder().isTruncated(true).nextContinuationToken("next")
                .contents(S3Object.builder().key("tana-place-images/Beach/a.jpg").build()).build(),
            ListObjectsV2Response.builder().isTruncated(false)
                .contents(S3Object.builder().key("tana-place-images/Beach/b.jpg").build()).build());
        CommonUtils utils = new CommonUtils();
        ReflectionTestUtils.setField(utils, "s3Client", s3);
        ReflectionTestUtils.setField(utils, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(utils, "cacheManager", new ConcurrentMapCacheManager("spot-images"));
        assertEquals(2, utils.getSpotImages("Beach").size());
        verify(s3).listObjectsV2(argThat((ListObjectsV2Request request) ->
            "next".equals(request.continuationToken()) && request.prefix().equals("tana-place-images/Beach/")));
    }
}
