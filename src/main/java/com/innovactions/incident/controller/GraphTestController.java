package com.innovactions.incident.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.innovactions.incident.adapter.inbound.email.EmailMessageFetcher;

@RestController
@RequestMapping("/api/test")
public class GraphTestController {

    private final EmailMessageFetcher fetcher;

    public GraphTestController(EmailMessageFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @GetMapping("/message/{id}")
    public ResponseEntity<?> getMessage(@PathVariable String id) {
        try {
            var message = fetcher.fetchMessageDetails(id);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
