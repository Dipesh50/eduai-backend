package com.eduai.eduai_backend.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String college;
    private String branch;
    private Integer graduationYear;
}