package com.tana.tana_auth.functions.places.controller;

import com.tana.tana_auth.functions.places.dto.*;
import com.tana.tana_auth.functions.places.service.PlacesService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping(value = "/places")
public class PlacesController {

    @Autowired
    private PlacesService placesService;

    @PostMapping(value = "/create")
    public TanaApiResponse createPlaces(@RequestAttribute("validated") PlacesRequestDto requestDto) {
        placesService.createPlaces(requestDto);
        return TanaApiResponse.builder()
                .isSuccess(true)
                .build();
    }

    @PostMapping(value = "/dashboard")
    public TanaApiResponse fetchDashboard(@RequestAttribute("validated") DashboardRequestDto requestDto) {
        return TanaApiResponse.builder()
                .resultData(placesService.fetchDashboardImages(requestDto))
                .isSuccess(true)
                .build();
    }

    @PostMapping(value = "/get-all")
    public TanaApiResponse fetchAllPlaces(@RequestAttribute("validated") ExploreMapRequestDto requestDto) {
        return TanaApiResponse.builder()
                .resultData(placesService.fetchAllPlaces(requestDto))
                .isSuccess(true)
                .build();
    }

    @PostMapping(value = "/{id}")
    public TanaApiResponse fetchPlaceDetails(@PathVariable("id") String id) {
        return TanaApiResponse.builder()
                .resultData(placesService.fetchPlaceDetails(id))
                .isSuccess(true)
                .build();
    }

    @PutMapping(value = "/visited")
    public TanaApiResponse markVisited(@RequestAttribute("validated") VisitedDto requestDto) {
        return TanaApiResponse.builder()
                .resultData(placesService.markVisited(requestDto))
                .isSuccess(true)
                .build();
    }

    @PostMapping(value = "/add-reflections")
    public TanaApiResponse addReflection(
            @RequestParam("content") String content,
            @RequestParam("placeId") Long placeId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        placesService.addReflection(content, placeId, file);
        return TanaApiResponse.builder()
                .isSuccess(true)
                .build();
    }

    @PostMapping(value = "/suggest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TanaApiResponse suggestSpot(
        @RequestParam("spotName") String spotName,
        @RequestParam("category") String category,
        @RequestParam("location") String location,
        @RequestParam("vibe") String vibe,
        @RequestParam(value = "photos", required = false) MultipartFile[] photos
    ) {
        System.out.println("spotName: " + spotName);

        if (photos != null) {
            for (MultipartFile file : photos) {
                System.out.println("File: " + file.getOriginalFilename());
                System.out.println("Size: " + file.getSize());
            }
        }
        placesService.suggestSpot(spotName, category, location, vibe, photos);

        return TanaApiResponse.builder().isSuccess(true).build();
    }


    @PostMapping(value = "/get-reflections")
    private TanaApiResponse fetchReflections(
            @RequestAttribute("validated") ReflectionRequestDto requestDto
    ) throws TanaException {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(placesService.getReflections(requestDto))
                .build();
    }

    @PostMapping(value = "/save")
    private TanaApiResponse save(
            @RequestAttribute("validated") SaveRequestDto requestDto
    ) throws TanaException {
        placesService.saveSpotOrCollection(requestDto);
        return TanaApiResponse.builder()
                .isSuccess(true)
                .build();
    }

    @PostMapping(value = "/saved-collections")
    private TanaApiResponse getSavedCollections(
    ) throws TanaException {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(placesService.fetchSavedCollections())
                .build();
    }

    @GetMapping("/images/{spotName}/{filename}")
    public ResponseEntity<UrlResource> getImage(
            @PathVariable String spotName,
            @PathVariable String filename) throws IOException {

        Path basePath = Paths.get("D:/tana-place-images");
        Path filePath = basePath.resolve(spotName).resolve(filename).normalize();

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
