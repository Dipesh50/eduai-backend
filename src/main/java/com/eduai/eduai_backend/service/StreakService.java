package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.dto.StreakDto;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.entity.UserStreak;
import com.eduai.eduai_backend.repository.StreakRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Daily check-in ────────────────────────────────────
    public StreakDto checkIn() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();

        UserStreak streak = streakRepository.findByUser(user)
                .orElse(UserStreak.builder()
                        .user(user)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalActiveDays(0)
                        .build());

        // Already checked in today
        if (today.equals(streak.getLastActiveDate())) {
            return buildDto(streak, true,
                    "Already checked in today! Keep it up!");
        }

        // Consecutive day — increment streak
        if (streak.getLastActiveDate() != null &&
                streak.getLastActiveDate()
                        .plusDays(1).equals(today)) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setTotalActiveDays(
                    streak.getTotalActiveDays() + 1);
        }
        // Missed a day — reset streak
        else if (streak.getLastActiveDate() != null &&
                !streak.getLastActiveDate()
                        .plusDays(1).equals(today)) {
            streak.setCurrentStreak(1);
            streak.setStreakStartDate(today);
            streak.setTotalActiveDays(
                    streak.getTotalActiveDays() + 1);
        }
        // First ever check-in
        else {
            streak.setCurrentStreak(1);
            streak.setStreakStartDate(today);
            streak.setTotalActiveDays(1);
        }

        // Update longest streak
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastActiveDate(today);
        streakRepository.save(streak);

        String message = streak.getCurrentStreak() == 1
                ? "Streak started! Come back tomorrow!"
                : "Day " + streak.getCurrentStreak()
                + " streak! You're on fire!";

        return buildDto(streak, false, message);
    }

    // ── Get streak status ─────────────────────────────────
    public StreakDto getStatus() {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();

        UserStreak streak = streakRepository.findByUser(user)
                .orElse(UserStreak.builder()
                        .user(user)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalActiveDays(0)
                        .build());

        // Check if streak is broken
        if (streak.getLastActiveDate() != null &&
                streak.getLastActiveDate()
                        .plusDays(1).isBefore(today)) {
            streak.setCurrentStreak(0);
            streakRepository.save(streak);
        }

        boolean checkedInToday =
                today.equals(streak.getLastActiveDate());
        String message = checkedInToday
                ? "Checked in today! Streak: "
                + streak.getCurrentStreak() + " days"
                : "Check in today to maintain your streak!";

        return buildDto(streak, checkedInToday, message);
    }

    private StreakDto buildDto(UserStreak streak,
                               boolean checkedInToday, String message) {
        return StreakDto.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .totalActiveDays(streak.getTotalActiveDays())
                .lastActiveDate(streak.getLastActiveDate())
                .checkedInToday(checkedInToday)
                .message(message)
                .build();
    }
}