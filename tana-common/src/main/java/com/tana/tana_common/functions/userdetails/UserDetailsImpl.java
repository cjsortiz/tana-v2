package com.tana.tana_common.functions.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tana.tana_common.constant.enums.AccessLevel;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.DropdownMaster;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {


    private static final long serialVersionUID = 1L;

    private long id;

    private String username;

    @JsonIgnore
    private String password;

    private boolean isIpBlocked;

    private boolean isActive;

    private String companyId;

    private String image;

    private String image64;

    private String displayName;

    private String fullName;

    private String emailAddress;

    private boolean adminAccess;

    private boolean isOnboarded;

    private String preferMood;

    @Enumerated(EnumType.STRING)
    @Column(name = "accessLevel", nullable = false)
    private AccessLevel accessLevel;

    private Collection<? extends GrantedAuthority> authorities;

    private List<DropdownMaster> dropdownMasterList;


    public static UserDetailsImpl build(AccountMaster user, Collection<? extends GrantedAuthority> authorities,
                                        String image64, List<DropdownMaster> dropdownMasterList) {
        boolean isAdmin = user.getAccessLevel() == AccessLevel.ADMIN;

        return UserDetailsImpl.builder()
                .id(user.getId())
                .adminAccess(isAdmin)
                .username(user.getUserName())
                .password(user.getPassword())
                .isIpBlocked(user.isIpBlocked())
                .isActive(user.isActive())
                .authorities(authorities)
                .image(user.getUserImage())
                .image64(image64)
                .accessLevel(user.getAccessLevel())
                .displayName(buildDisplayName(user))
                .fullName(String.join(" ", new String[]{user.getFirstName(), user.getLastName()}))
                .emailAddress(user.getEmail())
                .dropdownMasterList(dropdownMasterList)
                .isOnboarded(user.isOnboarded())
                .preferMood(user.getPreferMood())
                .build();
    }

    public UserDetailsImpl updateUserDetails(
        String preferMood,
        Boolean isOnboarded
    ) {

        if (preferMood != null) {
            this.preferMood = preferMood;
        }

        if (isOnboarded != null) {
            this.isOnboarded = isOnboarded;
        }

        return this;
    }

    private static String buildDisplayName(AccountMaster accountMaster) {
        // Make sure firstName and lastName are not empty
        String firstName = accountMaster.getFirstName();
        String lastName = accountMaster.getLastName();

        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return ""; // or some default
        }

        // Take first letters and capitalize

        return ""
                + Character.toUpperCase(firstName.charAt(0))
                + Character.toUpperCase(lastName.charAt(0))
                + Character.toUpperCase(lastName.charAt(0));
    }


    // Other fields like socials, vibe, offerings can go here

    // --- UserDetails methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // prefix ROLE_ is required by Spring Security convention
        return List.of(new SimpleGrantedAuthority("ROLE_" + accessLevel.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
