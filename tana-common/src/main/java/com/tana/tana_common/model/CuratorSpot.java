package com.tana.tana_common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "CuratorSpot",
    indexes = {
        @Index(name = "idx_curator_spot_place", columnList = "placeId"),
        @Index(name = "idx_curator_spot_active_order", columnList = "active, displayOrder")
    }
)
public class CuratorSpot extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long curatorSpotId;

    @ManyToOne
    @JoinColumn(name = "placeId", nullable = false)
    private PlaceMaster place;

    @Column(name = "displayOrder", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "proofLabel")
    private String proofLabel;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
