package com.tana.tana_common.util.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Objects;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {

    @Autowired
    PasswordUtil passwordUtil;

    @Value("${encryption.keyconfig}")
    private String key;

    @Value("${encryption.salt}")
    private String salt;

    @Override
    public String encode(CharSequence plainTextPassword) {
        try {
            assert plainTextPassword != null;
            String encrypted = passwordUtil.encrypt(plainTextPassword.toString(), key, salt);
            return encrypted;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean matches(CharSequence plainTextPassword, String passwordInDatabase) {
        return Objects.equals(encode(plainTextPassword), passwordInDatabase);
    }
}