package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardImageResponse {
    private Long id;
    private String name;
//    private List<String> imageStrings;
    private String priceRange;
    private Boolean isTanaVerified;
    private Double distance;
}