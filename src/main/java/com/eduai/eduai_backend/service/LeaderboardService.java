package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.dto.LeaderboardDto;
import com.eduai.eduai_backend.entity.QuizResult;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.entity.UserStreak;
import com.eduai.eduai_backend.repository.QuizResultRepository;
import com.eduai.eduai_backend.repository.StreakRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final StreakRepository streakRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // ── Global leaderboard ────────────────────────────────
    public LeaderboardDto getGlobalLeaderboard() {
        User currentUser = getCurrentUser();
        List<User> allUsers = userRepository.findAll();
        List<LeaderboardDto.LeaderboardEntry> entries =
                new ArrayList<>();

        for (User user : allUsers) {
            List<QuizResult> results = quizResultRepository
                    .findByUserOrderByTakenAtDesc(user);

            if (results.isEmpty()) continue;

            double avgScore = results.stream()
                    .mapToDouble(QuizResult::getScorePercentage)
                    .average()
                    .orElse(0);

            int streak = streakRepository.findByUser(user)
                    .map(UserStreak::getCurrentStreak)
                    .orElse(0);

            entries.add(LeaderboardDto.LeaderboardEntry.builder()
                    .studentName(user.getName())
                    .averageScore(
                            Math.round(avgScore * 10.0) / 10.0)
                    .quizzesTaken(results.size())
                    .currentStreak(streak)
                    .build());
        }

        // Sort by average score descending
        entries.sort((a, b) ->
                Double.compare(b.getAverageScore(),
                        a.getAverageScore()));

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        // Find current user rank
        int myRank = entries.stream()
                .filter(e -> e.getStudentName()
                        .equals(currentUser.getName()))
                .map(LeaderboardDto.LeaderboardEntry::getRank)
                .findFirst()
                .orElse(0);

        return LeaderboardDto.builder()
                .entries(entries)
                .myRank(myRank)
                .topic("All Topics")
                .build();
    }

    // ── Topic leaderboard ─────────────────────────────────
    public LeaderboardDto getTopicLeaderboard(String topic) {
        User currentUser = getCurrentUser();
        List<User> allUsers = userRepository.findAll();
        List<LeaderboardDto.LeaderboardEntry> entries =
                new ArrayList<>();

        for (User user : allUsers) {
            List<QuizResult> results = quizResultRepository
                    .findByUserAndTopicOrderByTakenAtDesc(
                            user, topic);

            if (results.isEmpty()) continue;

            double avgScore = results.stream()
                    .mapToDouble(QuizResult::getScorePercentage)
                    .average()
                    .orElse(0);

            entries.add(LeaderboardDto.LeaderboardEntry.builder()
                    .studentName(user.getName())
                    .topic(topic)
                    .averageScore(
                            Math.round(avgScore * 10.0) / 10.0)
                    .quizzesTaken(results.size())
                    .build());
        }

        entries.sort((a, b) ->
                Double.compare(b.getAverageScore(),
                        a.getAverageScore()));

        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        int myRank = entries.stream()
                .filter(e -> e.getStudentName()
                        .equals(currentUser.getName()))
                .map(LeaderboardDto.LeaderboardEntry::getRank)
                .findFirst()
                .orElse(0);

        return LeaderboardDto.builder()
                .entries(entries)
                .myRank(myRank)
                .topic(topic)
                .build();
    }
}