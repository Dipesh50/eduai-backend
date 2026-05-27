package com.eduai.eduai_backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private Long id;
    private String userMessage;
    private String aiResponse;
    private LocalDateTime createdAt;
}