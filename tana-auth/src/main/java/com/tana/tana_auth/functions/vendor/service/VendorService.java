package com.tana.tana_auth.functions.vendor.service;

import com.tana.tana_auth.functions.vendor.dto.*;
import com.tana.tana_common.constant.exception.TanaException;

import java.util.List;

public interface VendorService {
    AdminDashboardDto getAdminDashboard();

    List<AdminVendorDto> getAdminVendors();

    List<VendorSpotDto> getAdminSpotAnalytics();

    List<VendorApplicationResponseDto> getAdminApplications();

    VendorApplicationResponseDto submitApplication(VendorApplicationRequestDto requestDto) throws TanaException;

    List<VendorApplicationResponseDto> getPendingApplications();

    VendorInviteResponseDto approveApplication(Long applicationId, VendorApplicationReviewRequestDto requestDto)
        throws TanaException;

    VendorApplicationResponseDto followUpApplication(Long applicationId, VendorApplicationReviewRequestDto requestDto)
        throws TanaException;

    VendorApplicationResponseDto declineApplication(Long applicationId, VendorApplicationReviewRequestDto requestDto)
        throws TanaException;

    VendorInviteResponseDto getInvite(String token) throws TanaException;

    VendorInviteResponseDto acceptInvite(String token, VendorInviteAcceptRequestDto requestDto) throws TanaException;

    List<VendorOwnershipDto> getOwnerships();

    VendorOwnershipDto linkPlaceOwnership(VendorPlaceOwnershipRequestDto requestDto) throws TanaException;

    VendorDetailsDto getDetails() throws TanaException;

    List<VendorSpotDto> getSpots();

    VendorAnalyticsDto getAnalytics();

    void submitEditSuggestion(VendorEditSuggestionRequestDto requestDto) throws TanaException;

    void submitSeasonalFlags(VendorSeasonalFlagsRequestDto requestDto) throws TanaException;

    VendorScanResultDto previewScan(VendorScanRequestDto requestDto) throws TanaException;

    VendorScanResultDto confirmScan(VendorScanRequestDto requestDto) throws TanaException;
}
