package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuratorSpotResponseDto {
    private Long curatorSpotId;
    private Long placeId;
    private String placeName;
    private String town;
    private Integer displayOrder;
    private String proofLabel;
    private Boolean active;
}
