package com.tana.tana_common.util;

import com.tana.tana_common.constant.CommonConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Handles JWT related tasks.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.key}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-duration}")
    private long refreshExpiration;

    public JwtUtil() {
    }

    /**
     * Treat jwt.key as the configured secret text, rather than as an implicitly
     * Base64-encoded value. The deprecated String overload decodes the value as
     * Base64 and can therefore turn an otherwise long secret into an undersized
     * HS512 key.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token to be used for API requests from client.
     *
     * @param username The username of the user logging in
     * @param userType The type of user
     * @return JWT token as string
     */
    public String generateJwtToken(final String username, final String userType) {

        var date = new Date();
        var builder = Jwts.builder();
        String uid = UUID.randomUUID().toString();
        builder.setSubject(username + CommonConstants.USER_DELIMITER + uid);
        builder.setIssuedAt(date);
        builder.setExpiration(new Date((date).getTime() + expiration));

        builder.signWith(getSigningKey(), SignatureAlgorithm.HS512);
        return builder.compact();
    }

    public String generateRefreshToken(final String username, final String userType) {
        var date = new Date();

        return generateRefreshToken(username, userType, new Date((date).getTime() + refreshExpiration));
    }

    public String generateRefreshToken(final String username, final String userType, Date expiryDate) {
        var date = new Date();
        var builder = Jwts.builder();
        String uid = UUID.randomUUID().toString();
        builder.setSubject(userType + CommonConstants.USER_DELIMITER + uid);
        builder.setIssuedAt(date);
        builder.setExpiration(expiryDate);
        builder.signWith(getSigningKey(), SignatureAlgorithm.HS512);
        return builder.compact();
    }

    /**
     * Extract the username from the header token.
     *
     * @param token JWT token passed by the request.
     * @return Username of the user as {@link String}.
     */
    public String getSubjectFromJwtToken(String token) {
        var parser = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build();
        var parsed = parser.parseClaimsJws(token);
        var body = parsed.getBody();
        return body.getSubject();
    }

    /**
     * Validate if the JWT token.
     *
     * @param authToken The token to be validated
     * @return True if valid. Otherwise, false.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            var parser = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build();
            Jws<Claims> claims = parser.parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }


    public String extractUsername(String token) {

        final Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        String subject = claims.getSubject(); // username|uuid

        // Split using your delimiter
        String[] parts = subject.split(CommonConstants.USER_DELIMITER);

        return parts[0]; // username
    }
}
