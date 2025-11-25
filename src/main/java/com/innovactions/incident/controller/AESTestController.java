package com.innovactions.incident.controller;

import com.innovactions.incident.adapter.security.EncryptionAdapter;
import com.innovactions.incident.application.ConversationContextService;
import com.innovactions.incident.application.IncidentContext;
import com.innovactions.incident.domain.model.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/aes-test")
@RequiredArgsConstructor
public class AESTestController {
  private final EncryptionAdapter encryptionAdapter;
  private final ConversationContextService conversationContextService;

  @GetMapping("/aes")
  public String testEncryption() throws Exception {
    String phone = "+14155552671";
    String encrypted = encryptionAdapter.encrypt(phone);
    String decrypted = encryptionAdapter.decrypt(encrypted);
    return "Original: " + phone + "\nEncrypted: " + encrypted + "\nDecrypted: " + decrypted;
  }

    /**
     * Returns all OPEN incidents for a given reporter.
     * Example:
     *   GET /api/incidents/open?reporterId=123
     */
    @GetMapping("/open")
    public List<IncidentContext> checkIncidentListByReporterAndStatus(@RequestParam String reporterId){
        //Fixme: remove only for debugging List of incident with cannel id which are open by the reporter
return conversationContextService.getAllOpenIncidentByReporter(reporterId);
    }
}
