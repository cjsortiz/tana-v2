package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tana.tana_common.constant.enums.SegmentTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "CollectionsMaster",
        indexes = {
            @Index(name = "idx_cms_collection",
                columnList = "collectionId"),
            @Index(name = "idx_collection_name",
                columnList = "name"),
        }
)
public class CollectionsMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collectionId;

    @Column(name = "collectionName",length = 64,nullable = false, unique = true)
    private String collectionName;

    @Column(name = "overview")
    private String overview;

    @Column(name = "badgeOverview", columnDefinition = "TEXT")
    private String badgeOverview;

    @Column(name = "badge")
    private String badge;

    @Column(name = "helperText")
    private String helperText;

    @Column(name = "collectionImage")
    private String collectionImage;

    @Column(name = "moodType")
    private Integer moodType;

    @Column(name = "moodPriority")
    private Integer moodPriority;

    @ManyToOne
    @JoinColumn(name = "spot")
    @JsonBackReference
    private Spot spot;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL)
    private List<CollectionsCategorySelections> collection;

    @OneToMany(mappedBy = "collection")
    private List<UserSaves> userSaves;
}
