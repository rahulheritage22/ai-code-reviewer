package com.example.aicodereviewer.controller;

import com.example.aicodereviewer.service.GithubWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final GithubWebhookService webhookService;

    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String event,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload) {
        
        log.info("Received GitHub Webhook Event: {}", event);
        try {
            webhookService.handleWebhook(event, signature, payload);
            return ResponseEntity.ok("Webhook processed");
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payload");
        }
    }
}
