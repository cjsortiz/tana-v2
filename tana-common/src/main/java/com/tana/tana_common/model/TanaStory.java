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
    name = "TanaStory",
    indexes = {
        @Index(name = "idx_tana_story_active_order", columnList = "active, displayOrder")
    }
)
public class TanaStory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tanaStoryId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "tag")
    private String tag;

    @Column(name = "category")
    private String category;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "linkUrl", nullable = false, length = 1000)
    private String linkUrl;

    @Column(name = "displayOrder", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
