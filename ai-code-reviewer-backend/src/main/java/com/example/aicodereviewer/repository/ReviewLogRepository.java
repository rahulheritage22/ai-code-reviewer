package com.example.aicodereviewer.repository;

import com.example.aicodereviewer.model.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
}
