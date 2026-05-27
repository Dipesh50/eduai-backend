package com.eduai.eduai_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @PrePersist
    protected void onCreate() {
        takenAt = LocalDateTime.now();
    }
}