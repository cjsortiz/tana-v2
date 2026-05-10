package com.tana.tana_common.constant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PlacesRequestDto {

    private String name;

    private List<String> categoryTypeEnum;

    private List<String> subCategoryTypeEnum;

    private String priceRange;

    private String capacity;

    private List<String> vibe;

    private List<String> offerings;

    private String town;

    private String googleAddress;

    private Object socials;

    private Boolean isTanaVerified;

    private List<String> imageStrings;

}
