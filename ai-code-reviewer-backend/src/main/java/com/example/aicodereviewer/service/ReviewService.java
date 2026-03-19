package com.example.aicodereviewer.service;

import com.example.aicodereviewer.model.GithubRepo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final GithubApiService githubApiService;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

    @org.springframework.beans.factory.annotation.Value("${github-token:}")
    private String globalGithubToken;

    @Async
    public void startReview(GithubRepo repo, int prNumber) {
        log.info("Starting AI review for {} PR #{}", repo.getFullName(), prNumber);
        
        try {
            String token = (repo.getOwner() != null && repo.getOwner().getGithubAccessToken() != null) 
                    ? repo.getOwner().getGithubAccessToken() 
                    : globalGithubToken;

            String diff = githubApiService.fetchPullRequestDiff(repo.getFullName(), prNumber, token);
            
            if (diff == null || diff.isEmpty()) {
                log.info("No diff found for PR #{}", prNumber);
                return;
            }

            String prompt = "You are an expert Senior Software Engineer. Review the following Pull Request diff.\n\n" +
                    "Your entire response MUST be a valid JSON object. Do NOT wrap the JSON in markdown formatting blocks like ```json.\n" +
                    "CRITICAL JSON SYNTAX RULE: The 'summary', 'suggestion', and 'comment' fields are raw JSON strings. You MUST strictly escape all internal double quotes as \\\" and inner newlines as \\n.\n\n" +
                    "Format the JSON object exactly like this:\n" +
                    "{\n" +
                    "  \"summary\": \"A short description of the changes.\\n\\nSecurity Vulnerabilities: [Detail or 'None identified.']\\n\\nPerformance Bottlenecks: [Detail or 'None identified.']\\n\\nCode Style Improvements: [Detail or 'None identified.']\",\n" +
                    "  \"suggestions\": [\n" +
                    "    {\n" +
                    "      \"path\": \"src/main/java/MyClass.java\",\n" +
                    "      \"line\": 42,\n" +
                    "      \"suggestion\": \"The exact, complete replacement code snippet to insert.\",\n" +
                    "      \"comment\": \"A brief explanation of WHY this specific code change is recommended.\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "Note: Only include files and lines that actually need changes in the 'suggestions' array. If none, leave it empty.\n\n" +
                    "Here is the diff:\n\n" + diff;
            
            ChatClient chatClient = chatClientBuilder.build();
            String reviewComment = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Extract pure JSON if AI ignores instructions and wraps it in markdown
            if (reviewComment != null && reviewComment.trim().startsWith("```json")) {
                reviewComment = reviewComment.substring(reviewComment.indexOf("{"), reviewComment.lastIndexOf("```")).trim();
            }

            try {
                Map<String, Object> responseJson = mapper.readValue(reviewComment, new TypeReference<Map<String, Object>>() {});
                
                // 1. Post the general overview comment first
                String summary = (String) responseJson.get("summary");
                if (summary != null && !summary.isBlank()) {
                    githubApiService.postReviewComment(repo.getFullName(), prNumber, token, summary);
                }

                // 2. Post inline single-click suggestions
                Object suggestionsObj = responseJson.get("suggestions");
                if (suggestionsObj instanceof List) {
                    List<Map<String, Object>> suggestions = (List<Map<String, Object>>) suggestionsObj;
                    if (!suggestions.isEmpty()) {
                        String commitId = githubApiService.fetchPullRequestCommitSha(repo.getFullName(), prNumber, token);
                        for (Map<String, Object> sug : suggestions) {
                            String path = (String) sug.get("path");
                            int line = Integer.parseInt(sug.get("line").toString());
                            String suggestionText = (String) sug.get("suggestion");
                            String commentText = sug.containsKey("comment") ? (String) sug.get("comment") : "Suggested change:";
                            
                            githubApiService.postInlineReviewComment(repo.getFullName(), prNumber, token, commitId, path, line, suggestionText, commentText);
                        }
                        log.info("Successfully posted {} inline suggestions", suggestions.size());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse nested JSON object from AI, falling back to standard comment string. Raw output length: {}", reviewComment.length());
                githubApiService.postReviewComment(repo.getFullName(), prNumber, token, reviewComment);
            }
            
            log.info("Finished AI review for PR #{}", prNumber);
        } catch (Exception e) {
            log.error("Failed to execute review", e);
        }
    }
}
