package com.eduai.eduai_backend.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakDto {
    private int currentStreak;
    private int longestStreak;
    private int totalActiveDays;
    private LocalDate lastActiveDate;
    private boolean checkedInToday;
    private String message;
}