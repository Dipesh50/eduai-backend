package com.eduai.eduai_backend.repository;

import com.eduai.eduai_backend.entity.ResumeAnalysis;
import com.eduai.eduai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ResumeAnalysisRepository
        extends JpaRepository<ResumeAnalysis, Long> {

    List<ResumeAnalysis> findByUserOrderByAnalyzedAtDesc(User user);
    Optional<ResumeAnalysis> findTopByUserOrderByAnalyzedAtDesc(User user);
}