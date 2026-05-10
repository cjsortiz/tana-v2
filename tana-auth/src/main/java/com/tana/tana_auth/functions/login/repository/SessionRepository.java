package com.tana.tana_auth.functions.login.repository;

import com.tana.tana_common.model.SessionToken;
import jakarta.transaction.Transactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionToken, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM SessionToken " +
            "WHERE accountMaster = :userName",nativeQuery = true)
    void deleteByUserName(@Param("userName")Long userName);

    /**
     * Retrieve a Session entity by refresh token.
     *
     * @param refreshToken The refresh token associated with the session.
     * @return The Session entity if found, or null if not found.
     */
    @Query(value = "SELECT * FROM SessionToken WHERE refreshTokenString = :refreshToken", nativeQuery = true)
    SessionToken findByRefreshToken(String refreshToken);
}
