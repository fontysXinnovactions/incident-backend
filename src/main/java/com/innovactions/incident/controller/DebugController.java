package com.innovactions.incident.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
  @PostMapping("/slack/reporter")
  public String debugSlackRequest(
      @RequestHeader Map<String, String> headers, @RequestBody(required = false) String body) {

    System.out.println("=== DEBUG: Raw request received ===");
    System.out.println("Headers:");
    headers.forEach((key, value) -> System.out.println("  " + key + ": " + value));

    System.out.println("Body: " + (body != null ? body : "null"));
    System.out.println("=== END DEBUG ===");

    return "Debug reporter endpoint received request";
  }

  @PostMapping("/slack/manager")
  public String debugSlackManagerRequest(
      @RequestHeader Map<String, String> headers, @RequestBody(required = false) String body) {

    System.out.println("=== DEBUG: Raw manager request received ===");
    System.out.println("Headers:");
    headers.forEach((key, value) -> System.out.println("  " + key + ": " + value));

    System.out.println("Body: " + (body != null ? body : "null"));
    System.out.println("=== END DEBUG ===");

    return "Debug manager endpoint received request";
  }
}
