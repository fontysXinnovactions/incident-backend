package com.innovactions.incident.controller;

import com.innovactions.incident.adapter.security.EncryptionAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aes-test")
@RequiredArgsConstructor
public class AESTestController {
    private final EncryptionAdapter encryptionAdapter;

    @GetMapping("/aes")
    public String testEncryption() throws Exception {
        String phone = "+14155552671";
        String encrypted = encryptionAdapter.encrypt(phone);
        String decrypted = encryptionAdapter.decrypt(encrypted);
        return "Original: " + phone + "\nEncrypted: " + encrypted + "\nDecrypted: " + decrypted;
    }
}
