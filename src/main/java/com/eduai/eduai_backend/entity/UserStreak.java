package com.eduai.eduai_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_streaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int currentStreak;
    private int longestStreak;
    private int totalActiveDays;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "streak_start_date")
    private LocalDate streakStartDate;
}