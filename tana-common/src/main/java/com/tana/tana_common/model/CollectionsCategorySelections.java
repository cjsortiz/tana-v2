package com.tana.tana_common.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "CollectionsCategorySelections",
        indexes = {
                @Index(name = "idx_cms_collection_place",
                        columnList = "collectionId, placeId")
        }
)
public class CollectionsCategorySelections {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collectionSelectionId;


    // 🔗 Many selections → One Collection
    @ManyToOne
    @JoinColumn(name = "collectionId", nullable = false)
    private CollectionsMaster collection;

    // 🔗 Many selections → One Place
    @ManyToOne
    @JoinColumn(name = "placeId", nullable = false)
    private PlaceMaster place;

}
