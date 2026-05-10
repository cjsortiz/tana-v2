package com.tana.tana_common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

public class TanaCookieCsrfTokenRepositoryUtils implements CsrfTokenRepository {

    private final CookieCsrfTokenRepository delegate = CookieCsrfTokenRepository.withHttpOnlyFalse();

    /**
     * Generate a CSRF token
     */
    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    /**
     * Save the CSRF token
     */
    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        delegate.saveToken(token, request, response);

        // Modify the Set-Cookie header if needed
        String csrfCookieName = "XSRF-TOKEN";
        String csrfTokenValue = token.getToken() + "; Path=" + request.getContextPath();
        response.setHeader("Set-Cookie", String.format("%s=%s; Secure; HttpOnly; SameSite=Strict",
                csrfCookieName, csrfTokenValue));
    }

    /**
     * Load the CSRF token
     */
    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }
}
