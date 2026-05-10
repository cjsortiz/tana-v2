package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class VisitedDto {

    public Long collectionId;
    public Long placeId;
    public Boolean isVisited;
}
