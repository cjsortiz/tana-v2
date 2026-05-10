package com.tana.tana_auth.functions.login.service;

import com.tana.tana_common.model.SessionToken;
import org.springframework.stereotype.Service;

@Service
public interface SessionService {

    SessionToken clearOtherSessionAndSave(SessionToken sessionToken, String username);
    /**
     * Retrieve a Session entity by refresh token.
     *
     * @param refreshToken The refresh token associated with the session.
     * @return The Session entity if found, or null if not found.
     */
    SessionToken findByRefreshToken(final String refreshToken);

    /**
     * Deletes a Session entity from the repository.
     *
     * @param session The Session entity to delete.
     */
    void delete(final SessionToken session);
}
