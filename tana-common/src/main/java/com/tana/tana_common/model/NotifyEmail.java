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
@Table(name = "NotifyEmail")
public class NotifyEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notifyId;

    @Column(name = "email",nullable = false,unique = true)
    private String email;
}
