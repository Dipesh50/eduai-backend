package com.eduai.eduai_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String content;
    private String topic;
    private String tag;
}