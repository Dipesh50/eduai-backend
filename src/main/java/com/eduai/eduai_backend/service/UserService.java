package com.eduai.eduai_backend.service;


import com.eduai.eduai_backend.dto.DashboardDto;
import com.eduai.eduai_backend.dto.UpdateProfileRequest;
import com.eduai.eduai_backend.dto.UserProfileDto;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.NoteRepository;
import com.eduai.eduai_backend.repository.QuizResultRepository;
import com.eduai.eduai_backend.repository.StreakRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final NoteRepository noteRepository;
    private final StreakRepository streakRepository;

    // ── Helper ────────────────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // ── GET profile ───────────────────────────────────────
    public UserProfileDto getProfile() {
        User user = getCurrentUser();
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .college(user.getCollege())
                .branch(user.getBranch())
                .graduationYear(user.getGraduationYear())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ── UPDATE profile ────────────────────────────────────
    public UserProfileDto updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setName(request.getName());
        user.setCollege(request.getCollege());
        user.setBranch(request.getBranch());
        user.setGraduationYear(request.getGraduationYear());
        userRepository.save(user);
        return getProfile();
    }

    // ── GET dashboard ─────────────────────────────────────
    public DashboardDto getDashboard() {
        User user = getCurrentUser();

        String memberSince = user.getCreatedAt() != null
                ? user.getCreatedAt().format(
                DateTimeFormatter.ofPattern("MMM yyyy"))
                : "N/A";

        long quizCount = quizResultRepository
                .countByUser(user);

        long notesCount = noteRepository
                .countByUser(user);

        int currentStreak = streakRepository
                .findByUser(user)
                .map(s -> s.getCurrentStreak())
                .orElse(0);

        return DashboardDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .quizzesTaken((int) quizCount)
                .notesCount((int) notesCount)
                .currentStreak(currentStreak)
                .memberSince(memberSince)
                .build();
    }
}