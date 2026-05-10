package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacesListResponseDto {

    private List<PlacesDetailsResponseDto> placeList;
}
