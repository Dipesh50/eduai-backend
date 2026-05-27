package com.eduai.eduai_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewAnswerRequest {
    @NotBlank
    private String answer;
}