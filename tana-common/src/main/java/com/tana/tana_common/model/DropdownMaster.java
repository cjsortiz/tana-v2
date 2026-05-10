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
@Table(name = "DropdownMaster")
public class DropdownMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",length = 64,nullable = false)
    private String name;

    @Column(name = "value",columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    private List<String> value;
}
