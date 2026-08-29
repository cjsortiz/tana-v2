package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardDto {
    private Integer totalSpots;
    private Integer totalVendors;
    private Integer pendingApplications;
    private Integer totalStamps;
}
