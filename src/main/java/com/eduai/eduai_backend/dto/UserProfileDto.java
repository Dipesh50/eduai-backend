package com.eduai.eduai_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String college;
    private String branch;
    private Integer graduationYear;
    private LocalDateTime createdAt;
}