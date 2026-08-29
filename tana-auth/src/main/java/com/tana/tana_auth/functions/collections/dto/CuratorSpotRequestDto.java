package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuratorSpotRequestDto {
    private Long placeId;
    private Integer displayOrder;
    private String proofLabel;
    private Boolean active;
}
