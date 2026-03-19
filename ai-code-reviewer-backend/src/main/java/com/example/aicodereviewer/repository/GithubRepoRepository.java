package com.example.aicodereviewer.repository;

import com.example.aicodereviewer.model.GithubRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, Long> {
    Optional<GithubRepo> findByFullName(String fullName);
}
