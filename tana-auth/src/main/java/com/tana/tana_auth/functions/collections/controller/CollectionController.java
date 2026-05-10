package com.tana.tana_auth.functions.collections.controller;

import com.tana.tana_auth.functions.collections.dto.CollectionDetailsRequestDto;
import com.tana.tana_auth.functions.collections.service.CollectionService;
import com.tana.tana_auth.functions.collections.service.CollectionsCategorySelectionsService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping(value = "/collections")
public class CollectionController {

    @Autowired
    private CollectionsCategorySelectionsService collectionsCategorySelectionsService;

    @Autowired
    private CollectionService collectionService;


    @PostMapping(value = "/getList")
    private TanaApiResponse getCollectionsList(
    ){
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(collectionService.getCollectionsList())
                .build();
    }

    @PostMapping(value = "/details")
    private TanaApiResponse getCollectionDetails(
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
