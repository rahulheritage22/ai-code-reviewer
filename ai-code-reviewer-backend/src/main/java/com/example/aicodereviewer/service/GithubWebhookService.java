package com.example.aicodereviewer.service;

import com.example.aicodereviewer.model.GithubRepo;
import com.example.aicodereviewer.repository.GithubRepoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubWebhookService {

    private final GithubRepoRepository repoRepository;
    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;

    public void handleWebhook(String event, String signature, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String fullName = root.path("repository").path("full_name").asText();

            GithubRepo repo = repoRepository.findByFullName(fullName).orElse(null);
            if (repo == null || !repo.isReviewEnabled()) {
                log.info("Repository {} not found or review not enabled", fullName);
                return;
            }

            if (!verifySignature(payload, signature, repo.getWebhookSecret())) {
                log.error("Invalid Webhook Signature for repo {}", fullName);
                throw new SecurityException("Invalid signature");
            }

            if ("pull_request".equals(event)) {
                String action = root.path("action").asText();
                if ("opened".equals(action) || "synchronize".equals(action)) {
                    int prNumber = root.path("number").asInt();
                    log.info("Valid PR event received for {} PR #{}", fullName, prNumber);
                    reviewService.startReview(repo, prNumber);
                }
            }

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new RuntimeException("Error processing webhook", e);
        }
    }

    private boolean verifySignature(String payload, String signatureHeader, String secret) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        String signature = signatureHeader.substring(7);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hmacBytes.length * 2);
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(signature);
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }
}
