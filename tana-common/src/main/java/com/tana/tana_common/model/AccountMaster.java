package com.tana.tana_common.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tana.tana_common.constant.enums.AccessLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AccountMaster")
public class AccountMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userName",length = 64,nullable = false)
    private String userName;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "firstName",length = 50)
    private String firstName;

    @Column(name="middleName",length =  50)
    private String middleName;

    @Column(name="lastName",length =  50)
    private String lastName;

    @Column(name = "email",nullable = false,unique = true)
    private String email;

    @Column(name ="ipAddress")
    private String ipaddress;

    @Column(name = "isIpBlocked", columnDefinition = "TINYINT(1)")
    private boolean isIpBlocked;

    @Column(name = "isOnboarded", columnDefinition = "TINYINT(1)")
    private boolean isOnboarded;

    @Column(name = "isActive", columnDefinition = "TINYINT(1)")
    private boolean isActive;

    @Column(name ="preferLang")
    private String preferLang;

    @Column(name ="userLocation")
    private String userLocation;

    @Column(name ="preferMood")
    private String preferMood;

    @Column(name ="userType")
    private Integer userType;

    @Column(name = "userImage")
    private String userImage;

    @Column(name = "bio",length = 60)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "accessLevel", nullable = false)
    private AccessLevel accessLevel;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private List<SpotVisited> visitedSpots;

}
