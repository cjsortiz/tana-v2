package com.tana.tana_auth.functions.routes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RouteRequestDto {
    private Long routeId;
    private String routeName;
    private Long routeCategoryId;
    private Long routePartnerId;
    private String routeDuration;
    private String overview;
    private String helperText;
    private String routeImage;
    private List<String> tags;
}
