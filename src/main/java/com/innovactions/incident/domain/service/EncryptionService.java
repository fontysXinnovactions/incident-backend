package com.innovactions.incident.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionService {
    private final SecretKey secretKey;

    public EncryptionService() throws Exception {
        // Generate AES key
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            this.secretKey = keyGenerator.generateKey();
            log.info("AES key generated successfully.");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to initialize AES key generator", e);
        }
    }
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
            log.info("Encrypted text using AES: {}", encryptedString);
            return encryptedString;
        } catch (Exception e) {
            throw new IllegalStateException("AES encryption failed", e);
        }
    }
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            String decryptedString = new String(decryptedBytes);
            log.info("Decrypted text using AES: {}", decryptedString);
            return decryptedString;
        } catch (Exception e) {
            throw new IllegalStateException("AES decryption failed", e);
        }
    }

}
