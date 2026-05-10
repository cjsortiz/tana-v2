package com.tana.tana_common.util.password;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class PasswordUtil {

    private final PasswordValidator passwordValidator;
    private SecretKeyFactory secretKeyFactory;
    private IvParameterSpec ivSpec;
    private Cipher cipher;

    @PostConstruct
    public void initialize() throws NoSuchAlgorithmException, NoSuchPaddingException {
        secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] iv = {53, -85, -115, -10, 69, 5, -55, -48, -112, 93, -68, -29, 20, 88, -46, 63};
        ivSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    public String encrypt(final String password, final String secretKey, final String salt)
            throws GeneralSecurityException {

        var spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
        var tmp = secretKeyFactory.generateSecret(spec);
        var secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        String encrpyted =  Base64.getUrlEncoder().withoutPadding()
                .encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));

        return encrpyted;
    }


    public String decrypt(final String password, final String secretKey, final String salt)
            throws GeneralSecurityException {
        var spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
        var tmp = secretKeyFactory.generateSecret(spec);
        var secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        return new String(cipher.doFinal(Base64.getUrlDecoder().decode(password)));
    }

    public boolean isValidPassword(final String password) {
        return passwordValidator.validate(new PasswordData(password)).isValid();
    }
}
