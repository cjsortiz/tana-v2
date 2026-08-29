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
    name = "RouteItinerary",
    indexes = {
        @Index(name = "idx_route_itinerary_route", columnList = "routeId"),
        @Index(name = "idx_route_itinerary_place", columnList = "placeId"),
        @Index(name = "idx_route_itinerary_order", columnList = "routeId, dayNumber, stopOrder")
    }
)
public class RouteItinerary extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeItineraryId;

    @ManyToOne
    @JoinColumn(name = "routeId", nullable = false)
    private RouteMaster route;

    @ManyToOne
    @JoinColumn(name = "placeId", nullable = false)
    private PlaceMaster place;

    @Column(name = "dayNumber", nullable = false)
    private Integer dayNumber;

    @Column(name = "stopOrder", nullable = false)
    private Integer stopOrder;

    @Column(name = "timeBlock", length = 64)
    private String timeBlock;

    @Column(name = "priority", length = 32)
    private String priority;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
