package com.eduai.eduai_backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisDto {

    private Long id;
    private String fileName;
    private int atsScore;
    private String overallGrade;
    private List<String> presentSkills;
    private List<String> missingSkills;
    private List<String> suggestions;
    private List<String> strengths;
    private LocalDateTime analyzedAt;
}