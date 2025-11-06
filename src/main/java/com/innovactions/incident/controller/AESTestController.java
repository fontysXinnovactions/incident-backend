package com.innovactions.incident.controller;

import com.innovactions.incident.domain.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aes-test")
@RequiredArgsConstructor
public class AESTestController {
    private final EncryptionService encryptionService;

    @GetMapping("/aes")
    public String testEncryption() throws Exception {
        String phone = "+14155552671";
        String encrypted = encryptionService.encrypt(phone);
        String decrypted = encryptionService.decrypt(encrypted);
        return "Original: " + phone + "\nEncrypted: " + encrypted + "\nDecrypted: " + decrypted;
    }
}
