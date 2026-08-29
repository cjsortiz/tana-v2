package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TanaStoryResponseDto {
    private Long tanaStoryId;
    private String title;
    private String tag;
    private String category;
    private String description;
    private String image;
    private String linkUrl;
    private Integer displayOrder;
    private Boolean active;
}
