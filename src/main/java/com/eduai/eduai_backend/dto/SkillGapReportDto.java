package com.eduai.eduai_backend.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapReportDto {
    private Long id;
    private String targetRole;
    private int readinessScore;
    private String readinessGrade;
    private List<String> presentSkills;
    private List<String> missingSkills;
    private List<String> learningRoadmap;
    private LocalDateTime createdAt;
}