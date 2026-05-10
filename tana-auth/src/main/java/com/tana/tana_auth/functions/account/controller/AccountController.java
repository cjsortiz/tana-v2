package com.tana.tana_auth.functions.account.controller;

import com.tana.tana_auth.functions.account.dto.AccountMasterRequestDto;
import com.tana.tana_auth.functions.account.service.AccountService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.util.CommonUtils;
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
@RequestMapping(value = "/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CommonUtils commonUtils;

    @PostMapping
    private TanaApiResponse getUserProfile() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(accountService.getUserProfileDetails())
            .build();
    }

    @PutMapping()
    private TanaApiResponse updateOnboardingStatus(@RequestAttribute("validated") AccountMasterRequestDto requestDto) {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(accountService.updateOnboardingStatus(requestDto))
            .build();
    }

    @PutMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    private TanaApiResponse updateProfile(
        AccountMasterRequestDto requestDto,
        @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        // Validate request
        requestDto = commonUtils.validateObjectData(requestDto);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(accountService.updateProfile(requestDto, file))
            .build();
    }

    @GetMapping("/images/{users}/{filename}")
    public ResponseEntity<UrlResource> getImage(
        @PathVariable String users,
        @PathVariable String filename
    ) throws IOException {

        Path basePath = Paths.get("D:/tana-users");
        Path filePath = basePath.resolve(users).resolve(filename).normalize();

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
            .header("Cache-Control", "public, max-age=31536000")
            .contentType(mediaType)
            .body(resource);
    }
}
