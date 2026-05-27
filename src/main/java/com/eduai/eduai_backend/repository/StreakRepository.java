package com.eduai.eduai_backend.repository;

import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface StreakRepository
        extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUser(User user);

    @Query("SELECT s FROM UserStreak s " +
            "ORDER BY s.currentStreak DESC")
    List<UserStreak> findTopStreaks();
}