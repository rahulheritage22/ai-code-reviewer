package com.example.aicodereviewer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private GithubRepo repo;

    private Integer pullRequestNumber;
    private String prTitle;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum ReviewStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
