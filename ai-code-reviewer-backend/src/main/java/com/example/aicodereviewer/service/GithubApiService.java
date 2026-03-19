package com.example.aicodereviewer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class GithubApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String fetchPullRequestDiff(String repoFullName, int prNumber, String userToken) {
        String url = String.format("https://api.github.com/repos/%s/pulls/%d", repoFullName, prNumber);
        
        HttpHeaders headers = new HttpHeaders();
        if (userToken != null && !userToken.isBlank()) {
            headers.set("Authorization", "Bearer " + userToken);
        }
        headers.set("Accept", "application/vnd.github.v3.diff");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch PR diff for {} #{}", repoFullName, prNumber, e);
            throw new RuntimeException("Could not fetch PR Diff", e);
        }
    }

    public String fetchPullRequestCommitSha(String repoFullName, int prNumber, String userToken) {
        String url = String.format("https://api.github.com/repos/%s/pulls/%d", repoFullName, prNumber);
        
        HttpHeaders headers = new HttpHeaders();
        if (userToken != null && !userToken.isBlank()) {
            headers.set("Authorization", "Bearer " + userToken);
        }
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return mapper.readTree(response.getBody()).path("head").path("sha").asText();
        } catch (Exception e) {
            log.error("Failed to fetch PR details for {} #{}", repoFullName, prNumber, e);
            throw new RuntimeException("Could not fetch PR details", e);
        }
    }

    public void postInlineReviewComment(String repoFullName, int prNumber, String userToken, String commitId, String path, int line, String suggestion, String comment) {
        String url = String.format("https://api.github.com/repos/%s/pulls/%d/comments", repoFullName, prNumber);

        HttpHeaders headers = new HttpHeaders();
        if (userToken != null && !userToken.isBlank()) {
            headers.set("Authorization", "Bearer " + userToken);
        }
        headers.set("Accept", "application/vnd.github.v3+json");

        try {
            String formattedSuggestion = comment + "\n\n```suggestion\n" + suggestion + "\n```";
            java.util.Map<String, Object> bodyMap = java.util.Map.of(
                "body", formattedSuggestion,
                "commit_id", commitId,
                "path", path,
                "line", line,
                "side", "RIGHT"
            );
            
            String jsonBody = mapper.writeValueAsString(bodyMap);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Posted inline suggestion to PR #{} on path {} line {}", prNumber, path, line);
        } catch (Exception e) {
            log.error("Failed to post inline comment to PR", e);
        }
    }

    public void postReviewComment(String repoFullName, int prNumber, String userToken, String comment) {
        String url = String.format("https://api.github.com/repos/%s/issues/%d/comments", repoFullName, prNumber);

        HttpHeaders headers = new HttpHeaders();
        if (userToken != null && !userToken.isBlank()) {
            headers.set("Authorization", "Bearer " + userToken);
        }
        headers.set("Accept", "application/vnd.github.v3+json");

        try {
            java.util.Map<String, String> bodyMap = java.util.Map.of("body", comment);
            String jsonBody = mapper.writeValueAsString(bodyMap);
            
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Posted review comment to PR #{} on {}", prNumber, repoFullName);
        } catch (Exception e) {
            log.error("Failed to post comment to PR", e);
        }
    }
}
