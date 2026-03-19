package com.example.aicodereviewer.controller;

import com.example.aicodereviewer.model.GithubRepo;
import com.example.aicodereviewer.repository.GithubRepoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repos")
public class GithubRepoController {

    private final GithubRepoRepository repoRepository;

    public GithubRepoController(GithubRepoRepository repoRepository) {
        this.repoRepository = repoRepository;
    }

    @GetMapping
    public List<GithubRepo> getAllRepos() {
        return repoRepository.findAll();
    }

    @PostMapping
    public GithubRepo addRepo(@RequestBody GithubRepo repo) {
        if (repo.getFullName() == null || repo.getWebhookSecret() == null) {
            throw new IllegalArgumentException("Full name and Webhook Secret are rigorously required for HMAC Webhook validation");
        }
        
        // Prevent duplicates
        repoRepository.findByFullName(repo.getFullName()).ifPresent(r -> {
            throw new IllegalArgumentException("Repository already monitored! Please simply update settings.");
        });
        
        repo.setReviewEnabled(true);
        return repoRepository.save(repo);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<GithubRepo> toggleRepo(@PathVariable Long id) {
        return repoRepository.findById(id).map(repo -> {
            repo.setReviewEnabled(!repo.isReviewEnabled());
            return ResponseEntity.ok(repoRepository.save(repo));
        }).orElse(ResponseEntity.notFound().build());
    }
}
