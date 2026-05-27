package com.eduai.eduai_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private String name;
    private String email;
    private int quizzesTaken;
    private int notesCount;
    private int currentStreak;
    private String memberSince;
}