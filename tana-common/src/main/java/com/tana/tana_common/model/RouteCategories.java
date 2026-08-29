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
    name = "RouteCategories",
    indexes = {
        @Index(name = "idx_route_category_name", columnList = "categoryName")
    }
)
public class RouteCategories extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeCategoryId;

    @Column(name = "categoryName", length = 64, nullable = false, unique = true)
    private String categoryName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "routeCategory")
    private List<RouteMaster> routes;
}
