package com.tana.tana_common.model;

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
    name = "RouteMaster",
    indexes = {
        @Index(name = "idx_route_name", columnList = "routeName"),
        @Index(name = "idx_route_category", columnList = "routeCategoryId"),
        @Index(name = "idx_route_partner", columnList = "routePartnerId")
    }
)
public class RouteMaster extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;

    @Column(name = "routeName", length = 128, nullable = false)
    private String routeName;

    @ManyToOne
    @JoinColumn(name = "routeCategoryId", nullable = false)
    private RouteCategories routeCategory;

    @ManyToOne
    @JoinColumn(name = "routePartnerId")
    private RoutePartners routePartner;

    @Column(name = "routeDuration", length = 64)
    private String routeDuration;

    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    @Column(name = "helperText")
    private String helperText;

    @Column(name = "routeImage")
    private String routeImage;

    @Column(name = "tags", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> tags;

    @OneToMany(mappedBy = "route")
    private List<RouteItinerary> itinerary;
}
