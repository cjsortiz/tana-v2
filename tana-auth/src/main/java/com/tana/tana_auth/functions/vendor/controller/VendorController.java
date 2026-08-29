package com.tana.tana_auth.functions.vendor.controller;

import com.tana.tana_auth.functions.vendor.dto.*;
import com.tana.tana_auth.functions.vendor.service.VendorService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/dashboard")
    public TanaApiResponse getAdminDashboard() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getAdminDashboard())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/vendors")
    public TanaApiResponse getAdminVendors() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getAdminVendors())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/spots")
    public TanaApiResponse getAdminSpotAnalytics() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getAdminSpotAnalytics())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/applications")
    public TanaApiResponse getAdminApplications() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getAdminApplications())
            .build();
    }

    @PostMapping(value = "/applications")
    public TanaApiResponse submitApplication(@RequestBody VendorApplicationRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.submitApplication(requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/applications/pending")
    public TanaApiResponse getPendingApplications() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getPendingApplications())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/applications/{applicationId}/approve")
    public TanaApiResponse approveApplication(
        @PathVariable Long applicationId,
        @RequestBody VendorApplicationReviewRequestDto requestDto
    ) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.approveApplication(applicationId, requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/applications/{applicationId}/follow-up")
    public TanaApiResponse followUpApplication(
        @PathVariable Long applicationId,
        @RequestBody VendorApplicationReviewRequestDto requestDto
    ) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.followUpApplication(applicationId, requestDto))
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/applications/{applicationId}/decline")
    public TanaApiResponse declineApplication(
        @PathVariable Long applicationId,
        @RequestBody VendorApplicationReviewRequestDto requestDto
    ) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.declineApplication(applicationId, requestDto))
            .build();
    }

    @GetMapping(value = "/invites/{token}")
    public TanaApiResponse getInvite(@PathVariable String token) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getInvite(token))
            .build();
    }

    @PostMapping(value = "/invites/{token}/accept")
    public TanaApiResponse acceptInvite(
        @PathVariable String token,
        @RequestBody VendorInviteAcceptRequestDto requestDto
    ) throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.acceptInvite(token, requestDto))
            .build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @GetMapping(value = "/ownerships")
    public TanaApiResponse getOwnerships() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getOwnerships())
            .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/ownerships")
    public TanaApiResponse linkPlaceOwnership(@RequestBody VendorPlaceOwnershipRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.linkPlaceOwnership(requestDto))
            .build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @GetMapping(value = "/me")
    public TanaApiResponse getDetails() throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getDetails())
            .build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @GetMapping(value = "/spots")
    public TanaApiResponse getSpots() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getSpots())
            .build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @GetMapping(value = "/analytics")
    public TanaApiResponse getAnalytics() {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.getAnalytics())
            .build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @PostMapping(value = "/edit-suggestions")
    public TanaApiResponse submitEditSuggestion(@RequestBody VendorEditSuggestionRequestDto requestDto)
        throws TanaException {
        vendorService.submitEditSuggestion(requestDto);
        return TanaApiResponse.builder().isSuccess(true).build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @PostMapping(value = "/seasonal-flags")
    public TanaApiResponse submitSeasonalFlags(@RequestBody VendorSeasonalFlagsRequestDto requestDto)
        throws TanaException {
        vendorService.submitSeasonalFlags(requestDto);
        return TanaApiResponse.builder().isSuccess(true).build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @PostMapping(value = "/scans/preview")
    public TanaApiResponse previewScan(@RequestBody VendorScanRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.previewScan(requestDto))
            .build();
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @PostMapping(value = "/scans/confirm")
    public TanaApiResponse confirmScan(@RequestBody VendorScanRequestDto requestDto)
        throws TanaException {
        return TanaApiResponse.builder()
            .isSuccess(true)
            .resultData(vendorService.confirmScan(requestDto))
            .build();
    }
}
