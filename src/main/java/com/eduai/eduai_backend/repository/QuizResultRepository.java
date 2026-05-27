package com.eduai.eduai_backend.repository;

import com.eduai.eduai_backend.entity.QuizResult;
import com.eduai.eduai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserOrderByTakenAtDesc(User user);
    List<QuizResult> findByUserAndTopicOrderByTakenAtDesc(User user, String topic);
    long countByUser(User user);
}