package com.eduai.eduai_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mock_interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String conversationHistory;

    private int questionCount;
    private boolean completed;
    private int confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String feedbackReport;

    @Column(columnDefinition = "TEXT")
    private String weakAreas;

    @Column(columnDefinition = "TEXT")
    private String strongPoints;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
}