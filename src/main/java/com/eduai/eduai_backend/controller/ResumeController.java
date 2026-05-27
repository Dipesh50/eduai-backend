package com.eduai.eduai_backend.controller;

import com.eduai.eduai_backend.dto.ResumeAnalysisDto;
import com.eduai.eduai_backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResumeAnalysisDto> analyzeResume(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Please upload a PDF file");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are supported");
        }

        return ResponseEntity.ok(resumeService.analyzeResume(file));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ResumeAnalysisDto>> getHistory() {
        return ResponseEntity.ok(resumeService.getMyHistory());
    }
}