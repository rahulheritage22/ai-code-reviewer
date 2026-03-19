package com.example.aicodereviewer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_repos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubRepo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_github_id")
    private User owner;

    private String webhookSecret;
    private boolean reviewEnabled;
}
