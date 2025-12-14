package com.innovactions.incident.adapter.security;

import com.innovactions.incident.port.outbound.EncryptionPort;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Responsible for AES encryption and decryption.
 * Uses AES/CBC/PKCS5Padding
 * CBC = Cipher Block Chaining that works on blocks of 16 bytes
 * PKCS5Padding = Padding scheme to ensure plaintext is a multiple of block size.
 * IV = Initialization Vector works  with CBC to ensure same plaintext encrypts differently each time.
 * Key in .env is in base64 format.
 *
 * <p>
 *
 * <ul>
 *   <li>Reads the Base64-encoded AES key from an environment variable.
 *   <li>Performs encryption and decryption using the AES algorithm.
 * </ul>
 */
@Slf4j
@Service
public class EncryptionAdapter implements EncryptionPort {

    @Value("${encryption.key}")
    private String base64Key;

    private SecretKeySpec secretKey;
    private final IvParameterSpec iv =
            new IvParameterSpec("1234567890123456".getBytes()); // 16 bytes fixed IV

    @PostConstruct
    private void init() {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    /**
     * Encrypts the given plaintext using AES/CBC/PKCS5Padding.
     *
     * @param plaintext
     * @return
     * @throws Exception
     */
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * Decrypts the given ciphertext using AES/CBC/PKCS5Padding.
     *
     * @param ciphertext
     * @return
     * @throws Exception
     */
    public String decrypt(String ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
