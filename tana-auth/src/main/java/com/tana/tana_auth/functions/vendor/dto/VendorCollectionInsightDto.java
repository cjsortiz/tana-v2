package com.tana.tana_auth.functions.vendor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorCollectionInsightDto {
    private Long collectionId;
    private String collectionName;
    private Integer spotCount;
    private Integer completionRate;
    private String usualPosition;
}
