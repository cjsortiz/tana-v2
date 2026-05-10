package com.tana.tana_auth.config;

import com.tana.tana_common.functions.userdetails.UserDetailsImpl;
import com.tana.tana_common.model.AccountMaster;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthConfig {

    public UserDetailsImpl getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        return (UserDetailsImpl) auth.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public void refreshUserDetails(
        String preferMood,
        Boolean isOnboarded
    ) {

        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl currentUser =
            (UserDetailsImpl) auth.getPrincipal();

        currentUser.updateUserDetails(
            preferMood,
            isOnboarded
        );

        UsernamePasswordAuthenticationToken newAuth =
            new UsernamePasswordAuthenticationToken(
                currentUser,
                auth.getCredentials(),
                currentUser.getAuthorities()
            );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}