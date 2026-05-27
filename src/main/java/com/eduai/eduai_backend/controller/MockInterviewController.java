package com.eduai.eduai_backend.controller;

import com.eduai.eduai_backend.dto.*;
import com.eduai.eduai_backend.service.MockInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    @PostMapping("/start")
    public ResponseEntity<InterviewResponseDto> startInterview(
            @Valid @RequestBody InterviewStartRequest request) {
        return ResponseEntity.ok(
                mockInterviewService.startInterview(request));
    }

    @PostMapping("/{interviewId}/answer")
    public ResponseEntity<InterviewResponseDto> submitAnswer(
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewAnswerRequest request) {
        return ResponseEntity.ok(
                mockInterviewService.submitAnswer(
                        interviewId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewFeedbackDto>> getHistory() {
        return ResponseEntity.ok(
                mockInterviewService.getMyHistory());
    }
}