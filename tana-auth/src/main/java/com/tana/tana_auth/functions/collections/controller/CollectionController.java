package com.tana.tana_auth.functions.collections.controller;

import com.tana.tana_auth.functions.collections.dto.CollectionCreateRequestDto;
import com.tana.tana_auth.functions.collections.dto.CollectionDetailsRequestDto;
import com.tana.tana_auth.functions.collections.dto.CuratorSpotRequestDto;
import com.tana.tana_auth.functions.collections.dto.TanaStoryRequestDto;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping(value = "/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final ObjectMapper objectMapper;

    public CollectionController(CollectionService collectionService, ObjectMapper objectMapper) {
        this.collectionService = collectionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/getList")
    public TanaApiResponse getCollectionsList() {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getCollectionsList())
                .build();
    }

    @GetMapping(value = "/home-v2")
    public TanaApiResponse getHomeV2(){
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getHomeV2())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/all")
    public TanaApiResponse getAdminCollectionOptions() {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getAdminCollectionOptions())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/create")
    public TanaApiResponse createCollection(@RequestBody CollectionCreateRequestDto requestDto) throws TanaException {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.createCollection(requestDto))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/curator-spots")
    public TanaApiResponse getAdminCuratorSpots() {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getAdminCuratorSpots())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/curator-spots")
    public TanaApiResponse saveCuratorSpot(@RequestBody CuratorSpotRequestDto requestDto) throws TanaException {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.saveCuratorSpot(requestDto))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/tana-stories")
    public TanaApiResponse getAdminTanaStories() {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getAdminTanaStories())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/tana-stories", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TanaApiResponse saveTanaStory(@RequestBody TanaStoryRequestDto requestDto) throws TanaException {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.saveTanaStory(requestDto))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TanaApiResponse createCollectionWithImages(
        @RequestPart("collection") String collectionJson,
        @RequestPart(value = "collectionImage", required = false) MultipartFile collectionImage,
        @RequestPart(value = "badgeImage", required = false) MultipartFile badgeImage
    ) throws TanaException, JsonProcessingException {
        CollectionCreateRequestDto requestDto = objectMapper.readValue(
            collectionJson,
            CollectionCreateRequestDto.class
        );
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(collectionService.createCollection(requestDto, collectionImage, badgeImage))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/tana-stories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TanaApiResponse saveTanaStoryWithImage(
        @RequestPart("story") String storyJson,
        @RequestPart("file") MultipartFile file
    ) throws TanaException, JsonProcessingException {
        TanaStoryRequestDto requestDto = objectMapper.readValue(storyJson, TanaStoryRequestDto.class);

        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.saveTanaStory(requestDto, file))
                .build();
    }

    @PostMapping(value = "/details")
    public TanaApiResponse getCollectionDetails(
            @RequestAttribute("validated") CollectionDetailsRequestDto collectionDetailsRequestDto
    ) throws TanaException {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getCollectionDetails(collectionDetailsRequestDto))
                .build();
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<UrlResource> getImage(
        @PathVariable String filename) throws IOException {

        Path basePath = Paths.get("D:/tana-collection");
        Path filePath = basePath.resolve(filename).normalize();

        if (!filePath.startsWith(basePath.normalize())) {
            return ResponseEntity.badRequest().build();
        }

        UrlResource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = java.nio.file.Files.probeContentType(filePath);
        MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
            .header("Cache-Control", "public, max-age=86400")
            .contentType(mediaType)
            .body(resource);
    }
}
