package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "SpotVisited",
        indexes = {
                @Index(name = "idx_sv_place_visitDate", columnList = "placeId, visitDate")
        }
)
public class SpotVisited {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long visitedId;

    @Column(name = "isVisited")
    private Boolean isVisited;

    // 👤 User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    @JsonBackReference
    private AccountMaster account;

    // 📍 Place
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placeId", nullable = false)
    @JsonBackReference
    private PlaceMaster place;

    @Column(name = "visitDate")
    private LocalDate visitDate;
}
