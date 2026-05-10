package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveRequestDto {

    private Long collectionId;
    private Long placeId;
    private Boolean isSaving;
}
