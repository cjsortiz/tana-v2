package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "UserCompletedRoute",
    uniqueConstraints = @UniqueConstraint(columnNames = {"accountId", "routeId"}),
    indexes = @Index(name = "idx_completed_route_account", columnList = "accountId,completedAt")
)
public class UserCompletedRoute extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userCompletedRouteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    @JsonBackReference
    private AccountMaster account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routeId", nullable = false)
    private RouteMaster route;

    @Column(name = "completedAt", nullable = false)
    private LocalDateTime completedAt;
}
