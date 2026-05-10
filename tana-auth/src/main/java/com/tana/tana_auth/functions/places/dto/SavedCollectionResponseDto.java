package com.tana.tana_auth.functions.places.dto;

import com.tana.tana_auth.functions.collections.dto.CollectionsResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SavedCollectionResponseDto {

    private List<CollectionsResponseDto> collectionsResponseDtoList;
}
