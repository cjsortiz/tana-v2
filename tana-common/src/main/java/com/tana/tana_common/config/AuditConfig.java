package com.tana.tana_common.config;

import com.tana.tana_common.functions.userdetails.UserDetailsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    private static final String SYSTEM_AUDITOR = "system";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of(SYSTEM_AUDITOR);
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                return Optional.ofNullable(userDetails.getUsername()).or(() -> Optional.of(SYSTEM_AUDITOR));
            }

            if (principal instanceof String username && !username.isBlank()) {
                return Optional.of(username);
            }

            String name = authentication.getName();
            if (name != null && !name.isBlank()) {
                return Optional.of(name);
            }

            return Optional.of(SYSTEM_AUDITOR);
        };
    }
}
