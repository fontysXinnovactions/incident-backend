package com.innovactions.incident.adapter.security;

import com.innovactions.incident.port.outbound.EncryptionPort;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Responsible for AES encryption and decryption.
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

  @Value("${app.crypto.key}")
  private String keyString;

  private SecretKey secretKey;

  private SecretKey getSecretKey() {
    if (secretKey == null) {
      if (keyString == null || keyString.isBlank()) {
        throw new IllegalStateException("APP_CRYPTO_KEY is missing or empty!");
      }

      byte[] keyBytes = Base64.getDecoder().decode(keyString);
      if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
        throw new IllegalStateException("Invalid AES key length: must be 16, 24, or 32 bytes.");
      }

      secretKey = new SecretKeySpec(keyBytes, "AES");
      log.info("Encryption key initialized.");
    }
    return secretKey;
  }

  @Override
  public String encrypt(String plainText) {
    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
      byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
      String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
      log.info("Encrypted text using AES: {}", encryptedString);
      return encryptedString;
    } catch (Exception e) {
      throw new IllegalStateException("AES encryption failed", e);
    }
  }

  @Override
  public String decrypt(String encryptedText) {
    try {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
      byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
      String decryptedString = new String(decryptedBytes);
      log.info("Decrypted text using AES: {}", decryptedString);
      return decryptedString;
    } catch (Exception e) {
      throw new IllegalStateException("AES decryption failed", e);
    }
  }
}
