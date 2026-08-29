package com.tana.tana_auth.functions.collections.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopResponseDto {
    private String when;
    private String priority;
    private String name;
    private String description;
    private String tip;
    private Boolean verified;
}
