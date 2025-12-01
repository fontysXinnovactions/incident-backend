package com.innovactions.incident.port.outbound;

public interface EncryptionPort {
  String encrypt(String plainText);

  String decrypt(String encryptedText);
}
