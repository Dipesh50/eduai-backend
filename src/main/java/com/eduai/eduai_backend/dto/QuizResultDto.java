package com.eduai.eduai_backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private Long id;
    private String topic;
    private int totalQuestions;
    private int correctAnswers;
    private double scorePercentage;
    private String grade;
    private List<QuestionFeedback> feedback;
    private LocalDateTime takenAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionFeedback {
        private Long questionId;
        private String questionText;
        private String yourAnswer;
        private String correctAnswer;
        private boolean isCorrect;
        private String aiExplanation;
    }
}