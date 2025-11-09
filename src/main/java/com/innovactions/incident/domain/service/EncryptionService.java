package com.innovactions.incident.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data using AES/CBC/PKCS5Padding.
 * AES = Advanced Encryption Standard.
 * CBC = Cipher Block Chaining that works on blocks of 16 bytes.
 * PKCS5Padding = Padding scheme to ensure plaintext is a multiple of block size.
 * IV = Initialization Vector works with CBC to ensure same plaintext encrypts differently each time.
 * Key in .env is in base64 format.
 */
@Slf4j
@Component
public class EncryptionService {
    @Value("${encryption.key}")
    private String base64Key;

    private SecretKeySpec secretKey;
    private final IvParameterSpec iv = new IvParameterSpec("1234567890123456".getBytes()); // 16 bytes fixed IV

    @PostConstruct
    private void init() {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    /**
     * Encrypts the given plaintext using AES/CBC/PKCS5Padding.
     * @param plaintext
     * @return
     * @throws Exception
     */
    public String encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Decrypts the given ciphertext using AES/CBC/PKCS5Padding.
     * @param ciphertext
     * @return
     * @throws Exception
     */
    public String decrypt(String ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
}