package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "Spot")
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    @Column(name = "spotName", nullable = false )
    private String spotName;

    @Column(name = "description", nullable = false )
    private String description;

    @OneToMany(mappedBy = "spot", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<CollectionsMaster> collections;

}
