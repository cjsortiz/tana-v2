package com.tana.tana_common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SessionToken")
public class SessionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tokenString")
    private String tokenString;

    @Column(name = "refreshTokenString")
    private String refreshTokenString;


    @Column(name = "startTime")
    private LocalDateTime startTime;

    @Column(name = "expiry")
    private LocalDateTime expiry;

    @Column(name = "userName")
    private String userName;

    @OneToOne
    @JoinColumn(name = "accountMaster", referencedColumnName = "id")
    public AccountMaster accountMaster;
}
