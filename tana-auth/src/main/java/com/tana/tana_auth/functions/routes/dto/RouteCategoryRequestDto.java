package com.tana.tana_auth.functions.routes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RouteCategoryRequestDto {
    private String categoryName;
    private String description;
}
