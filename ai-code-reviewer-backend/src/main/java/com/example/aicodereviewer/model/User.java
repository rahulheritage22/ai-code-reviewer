package com.example.aicodereviewer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String githubId;

    private String name;
    private String email;
    private String avatarUrl;

    @Column(length = 1000)
    private String githubAccessToken;
}
