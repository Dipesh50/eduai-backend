package com.eduai.eduai_backend.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {
    private Long id;
    private String title;
    private String content;
    private String topic;
    private String tag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}