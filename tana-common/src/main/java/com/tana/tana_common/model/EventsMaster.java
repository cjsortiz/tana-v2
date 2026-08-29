package com.tana.tana_common.model;

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
@Table(name = "EventsMaster")
public class EventsMaster extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eventSlug", nullable = false, unique = true, length = 96)
    private String eventSlug;

    @Column(name = "eventName", nullable = false)
    private String eventName;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "dateLabel", length = 128)
    private String dateLabel;

    @Column(name = "monthSort", length = 64)
    private String monthSort;

    @Column(name = "monthKey", length = 16)
    private String monthKey;

    @Column(name = "location", length = 128)
    private String location;

    @Column(name = "gps", length = 64)
    private String gps;

    @Column(name = "frequency", length = 64)
    private String frequency;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "tanaTip", columnDefinition = "TEXT")
    private String tanaTip;

    @Column(name = "badge", length = 128)
    private String badge;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "hiddenTags", columnDefinition = "TEXT")
    private String hiddenTags;

    @Column(name = "accentColor", length = 16)
    private String accentColor;

    @Column(name = "tagBg", length = 48)
    private String tagBg;

    @Column(name = "tagTextColor", length = 16)
    private String tagTextColor;

    @Column(name = "featured", columnDefinition = "TINYINT(1)")
    private Boolean featured;

    @Column(name = "startsOn")
    private LocalDate startsOn;

    @Column(name = "endsOn")
    private LocalDate endsOn;
}
