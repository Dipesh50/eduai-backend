package com.eduai.eduai_backend.repository;


import com.eduai.eduai_backend.entity.MockInterview;
import com.eduai.eduai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MockInterviewRepository extends JpaRepository<MockInterview, Long> {

    List<MockInterview> findByUserOrderByStartedAtDesc(User user);

    Optional<MockInterview> findByIdAndUser(Long id, User user);

    List<MockInterview> findByUserAndCompleted(
            User user, boolean completed);
}