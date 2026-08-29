package com.tana.tana_auth.functions.login.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class AppleClientSecretService {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.team-id:}")
    private String teamId;

    @Value("${apple.key-id:}")
    private String keyId;

    @Value("${apple.private-key:}")
    private String privateKey;

    @Value("${apple.private-key-path:}")
    private String privateKeyPath;

    private volatile CachedSecret cachedSecret;

    public String getClientSecret() {
        Instant now = Instant.now();
        CachedSecret current = cachedSecret;
        if (current != null && current.refreshAfter().isAfter(now)) {
            return current.value();
        }

        synchronized (this) {
            current = cachedSecret;
            if (current == null || !current.refreshAfter().isAfter(now)) {
                cachedSecret = createClientSecret(now);
            }
            return cachedSecret.value();
        }
    }

    private CachedSecret createClientSecret(Instant issuedAt) {
        requireConfiguration(teamId, "APPLE_TEAM_ID");
        requireConfiguration(keyId, "APPLE_KEY_ID");
        requireConfiguration(clientId, "APPLE_CLIENT_ID");

        try {
            Instant expiresAt = issuedAt.plus(150, ChronoUnit.DAYS);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .subject(clientId)
                    .audience(APPLE_AUDIENCE)
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .build();
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build(),
                    claims);
            jwt.sign(new ECDSASigner(loadPrivateKey()));
            return new CachedSecret(jwt.serialize(), issuedAt.plus(120, ChronoUnit.DAYS));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate the Apple client secret", exception);
        }
    }

    private ECPrivateKey loadPrivateKey() throws Exception {
        String pem = privateKey;
        if (!StringUtils.hasText(pem) && StringUtils.hasText(privateKeyPath)) {
            pem = Files.readString(Path.of(privateKeyPath), StandardCharsets.UTF_8);
        }
        requireConfiguration(pem, "APPLE_PRIVATE_KEY or APPLE_PRIVATE_KEY_PATH");

        String normalized = pem.replace("\\n", "\n").trim();
        if (!normalized.contains("BEGIN PRIVATE KEY")) {
            normalized = new String(Base64.getDecoder().decode(normalized), StandardCharsets.UTF_8);
        }
        String encoded = normalized
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(encoded);
        return (ECPrivateKey) KeyFactory.getInstance("EC")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private void requireConfiguration(String value, String environmentName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(environmentName + " is required for Sign in with Apple");
        }
    }

    private record CachedSecret(String value, Instant refreshAfter) {
    }
}
