package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuggestSpotRequestDto {

    private String spotName;
    private String category;
    private String location;
    private String vibe;

}
