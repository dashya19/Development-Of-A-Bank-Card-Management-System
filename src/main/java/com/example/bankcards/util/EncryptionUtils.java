package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptionUtils {
    @Value("${app.encryption.secret-key}")
    private String secretKey;

    @Value("${app.encryption.salt}")
    private String salt;

    public String encrypt(String data) {
        try {
            SecretKeySpec key = generateKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec key = generateKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    private SecretKeySpec generateKey() {
        String keyWithSalt = secretKey + salt;
        byte[] keyBytes = new byte[16];
        byte[] originalBytes = keyWithSalt.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(originalBytes, 0, keyBytes, 0, Math.min(originalBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
