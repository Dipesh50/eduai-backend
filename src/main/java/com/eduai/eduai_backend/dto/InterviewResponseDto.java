package com.eduai.eduai_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponseDto {
    private Long interviewId;
    private String question;
    private int questionNumber;
    private boolean isCompleted;
    private InterviewFeedbackDto feedback;
}