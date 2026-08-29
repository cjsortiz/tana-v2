package com.tana.tana_common.model;

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
    name = "RoutePartners",
    indexes = {
        @Index(name = "idx_route_partner_name", columnList = "partnerName"),
        @Index(name = "idx_route_partner_active", columnList = "active")
    }
)
public class RoutePartners extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routePartnerId;

    @Column(name = "partnerName", length = 160, nullable = false, unique = true)
    private String partnerName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logoImage", length = 512)
    private String logoImage;

    @Column(name = "helperText")
    private String helperText;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "routePartner")
    private List<RouteMaster> routes;
}
