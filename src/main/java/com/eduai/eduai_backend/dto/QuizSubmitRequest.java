package com.eduai.eduai_backend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmitRequest {
    private String topic;
    // key = questionId, value = student's answer (A/B/C/D)
    private Map<Long, String> answers;
}