package com.example.aicodereviewer.controller;

import com.example.aicodereviewer.model.GithubRepo;
import com.example.aicodereviewer.repository.GithubRepoRepository;
import com.example.aicodereviewer.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Loads application-test.yml
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GithubRepoRepository githubRepoRepository;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void testPullRequestOpenedWebhookInvalidSignature() throws Exception {
        GithubRepo repo = new GithubRepo();
        repo.setFullName("test/repo");
        repo.setWebhookSecret("mysecret");
        repo.setReviewEnabled(true);
        Mockito.when(githubRepoRepository.findByFullName("test/repo")).thenReturn(Optional.of(repo));

        String payload = "{\"action\":\"opened\",\"number\":42,\"repository\":{\"full_name\":\"test/repo\"}}";

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-Hub-Signature-256", "sha256=invalid123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPullRequestOpenedWebhookValidSignature() throws Exception {
        GithubRepo repo = new GithubRepo();
        repo.setFullName("test/repo");
        repo.setWebhookSecret("mysecret");
        repo.setReviewEnabled(true);
        Mockito.when(githubRepoRepository.findByFullName("test/repo")).thenReturn(Optional.of(repo));

        String payload = "{\"action\":\"opened\",\"number\":42,\"repository\":{\"full_name\":\"test/repo\"}}";

        // Generate Valid HMAC
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec("mysecret".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder("sha256=");
        for (byte b : hmacBytes) {
            sb.append(String.format("%02x", b));
        }
        String validSignature = sb.toString();

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-Hub-Signature-256", validSignature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
                
        // Verify reviewService was called to start the test
        Mockito.verify(reviewService, Mockito.times(1)).startReview(repo, 42);
    }
}
