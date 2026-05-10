package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tana.tana_common.constant.enums.MainCategoryTypeEnum;
import com.tana.tana_common.constant.enums.SubCategoryTypeEnum;
import com.tana.tana_common.util.converter.JsonListConverter;
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
    name = "PlaceMaster",
    indexes = {
        @Index(name = "idx_place_main_category", columnList = "mainCategoryTypeEnum"),
        @Index(name = "idx_place_name", columnList = "name"),
        @Index(name = "idx_place_town", columnList = "town"),
    }
)
public class PlaceMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "mainCategoryTypeEnum")
    private MainCategoryTypeEnum mainCategoryTypeEnum;

    @Column(name = "subCategoryTypeEnum", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<SubCategoryTypeEnum> subCategoryTypeEnum;

    @Column(name = "priceRange", length = 64)
    private String priceRange;

    @Column(name = "capacity", length = 64)
    private String capacity;

    @Column(name = "vibe", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> vibe;

    @Column(name = "location", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> location;

    @Column(name = "town", length = 64)
    private String town;

    @Column(name = "googleAddress", length = 64)
    private String googleAddress;

    @Column(name = "facebook")
    private String facebook;

    @Column(name = "instagram")
    private String instagram;

    @Column(name = "isTanaVerified", columnDefinition = "TINYINT(1)")
    private Boolean isTanaVerified;

    @Column(name = "gpsLocation")
    private String gpsLocation;

    @Column(name = "openingHours")
    private String openingHours;

    @Column(name = "openingDays")
    private String openingDays;

    @Column(name = "overview",columnDefinition = "TEXT")
    private String overview;

    @Column(name = "tanaTip")
    private String tanaTip;

    @Column(name = "imageStrings", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> imageStrings;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<CollectionsCategorySelections> collections;

    @OneToMany(mappedBy = "place")
    private List<SpotVisited> visitors;

    @OneToMany(mappedBy = "place")
    private List<UserSaves> userSaves;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<Reflection> reflections;

}
