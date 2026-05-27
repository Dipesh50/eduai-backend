package com.eduai.eduai_backend.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFeedbackDto {
    private Long interviewId;
    private String topic;
    private int confidenceScore;
    private String overallGrade;
    private String feedbackReport;
    private List<String> strongPoints;
    private List<String> weakAreas;
    private List<String> improvementTips;
}