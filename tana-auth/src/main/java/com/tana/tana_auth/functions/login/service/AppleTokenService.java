package com.tana.tana_auth.functions.login.service;

import com.tana.tana_common.constant.EncryptionProperties;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.util.password.PasswordUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AppleTokenService {

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    private final PasswordUtil passwordUtil;
    private final EncryptionProperties encryptionProperties;
    private final AppleClientSecretService clientSecretService;
    private final RestTemplate restTemplate = new RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${apple.client-id:com.app.tana}")
    private String clientId;

    public AppleTokenService(
            PasswordUtil passwordUtil,
            EncryptionProperties encryptionProperties,
            AppleClientSecretService clientSecretService
    ) {
        this.passwordUtil = passwordUtil;
        this.encryptionProperties = encryptionProperties;
        this.clientSecretService = clientSecretService;
    }

    public String exchangeAuthorizationCode(String authorizationCode) {
        if (ObjectUtils.isEmpty(authorizationCode)) {
            throw new TanaException(CustomCodeErrors.APPLE_SIGN_IN_FAILED);
        }

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecretService.getClientSecret());
            form.add("code", authorizationCode);
            form.add("grant_type", "authorization_code");

            Map<?, ?> response = restTemplate.postForObject(
                    APPLE_TOKEN_URL,
                    formRequest(form),
                    Map.class);
            Object refreshToken = response == null ? null : response.get("refresh_token");
            if (refreshToken == null) {
                throw new TanaException(CustomCodeErrors.APPLE_SIGN_IN_FAILED);
            }

            return passwordUtil.encrypt(
                    refreshToken.toString(),
                    encryptionProperties.getKeyConfig(),
                    encryptionProperties.getSalt());
        } catch (TanaException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new TanaException(CustomCodeErrors.APPLE_SIGN_IN_FAILED);
        }
    }

    public void revoke(String encryptedRefreshToken) {
        if (ObjectUtils.isEmpty(encryptedRefreshToken)) {
            return;
        }

        try {
            String refreshToken = passwordUtil.decrypt(
                    encryptedRefreshToken,
                    encryptionProperties.getKeyConfig(),
                    encryptionProperties.getSalt());

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecretService.getClientSecret());
            form.add("token", refreshToken);
            form.add("token_type_hint", "refresh_token");
            restTemplate.postForEntity(APPLE_REVOKE_URL, formRequest(form), Void.class);
        } catch (Exception ignored) {
            // Account deletion must still complete when Apple is temporarily unavailable.
        }
    }

    private HttpEntity<MultiValueMap<String, String>> formRequest(
            MultiValueMap<String, String> form
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(form, headers);
    }
}
