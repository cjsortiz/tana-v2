package com.tana.tana_auth.functions.collections.dto;

import com.tana.tana_common.model.CollectionsMaster;
import com.tana.tana_common.model.PlaceMaster;
import com.tana.tana_common.model.SpotVisited;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionsCategoryCustomQueryResponseDto {

    private Long collectionSelectionId;

    private CollectionsMaster collection;

    private PlaceMaster place;
}
