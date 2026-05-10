package com.tana.tana_common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Reflection")
public class Reflection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reflectionId;

    @ManyToOne
    @JoinColumn(name = "accountId", nullable = false)
    private AccountMaster account;

    // 📍 Place it belongs to
    @ManyToOne
    @JoinColumn(name = "placeId", nullable = false)
    private PlaceMaster place;

    // 📝 Content
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image")
    private String image;

    // ⏱ timestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}