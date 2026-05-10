package com.tana.tana_common.security;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Security configuration for this service.
 */
@Component
@RequiredArgsConstructor
public class SecurityConfiguration {

    /**
     * {@link UserDetailsService} Handles the user details related transactions.
     */
    private final UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates the authentication provider.
     *
     * @return {@link DaoAuthenticationProvider} object.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authProvider = getDaoProvider();

        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    /**
     * Creates an authentication manager bean to be used by controllers.
     *
     * @param authConfig {@link AuthenticationConfiguration} injected object.
     * @return {@link AuthenticationManager} set by the config.
     * @throws Exception Thrown by the {@link AuthenticationManager} getAuthenticationManager method.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Create a new instance of a DAO provider.
     *
     * @return A new {@link DaoAuthenticationProvider} instance. This improves testability of the
     * code.
     */
    DaoAuthenticationProvider getDaoProvider() {
        return new DaoAuthenticationProvider();
    }

}
