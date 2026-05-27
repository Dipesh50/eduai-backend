package com.eduai.eduai_backend.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDto {
    private List<LeaderboardEntry> entries;
    private int myRank;
    private String topic;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private int rank;
        private String studentName;
        private String topic;
        private double averageScore;
        private int quizzesTaken;
        private int currentStreak;
    }
}