package com.eduai.eduai_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewStartRequest {
    @NotBlank
    private String topic;
}