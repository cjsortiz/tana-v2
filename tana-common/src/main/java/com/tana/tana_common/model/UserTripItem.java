package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UserTripItem", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"accountId", "itemType", "itemId"})
})
public class UserTripItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTripItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    @JsonBackReference
    private AccountMaster account;

    @Column(name = "itemType", nullable = false, length = 32)
    private String itemType;

    @Column(name = "itemId", nullable = false, length = 255)
    private String itemId;

    @Column(name = "saved", columnDefinition = "TINYINT(1)")
    private Boolean saved;

    @Column(name = "going", columnDefinition = "TINYINT(1)")
    private Boolean going;
}
