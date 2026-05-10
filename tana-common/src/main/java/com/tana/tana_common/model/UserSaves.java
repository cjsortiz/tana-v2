package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "UserSaves",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"accountId", "placeId"}),
                @UniqueConstraint(columnNames = {"accountId", "collectionId"})
        })
public class UserSaves {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saveId;

    @Column(name = "saved")
    private Boolean saved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    @JsonBackReference
    private AccountMaster account;

    // 📍 Place
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placeId")
    @JsonBackReference
    private PlaceMaster place;

    // 📍 Place
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collectionId")
    @JsonBackReference
    private CollectionsMaster collection;

}
