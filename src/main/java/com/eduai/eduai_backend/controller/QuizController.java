package com.eduai.eduai_backend.controller;

import com.eduai.eduai_backend.dto.*;
import com.eduai.eduai_backend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Get all available topics
    @GetMapping("/topics")
    public ResponseEntity<List<String>> getTopics() {
        return ResponseEntity.ok(quizService.getTopics());
    }

    // Get fixed DB questions
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDto>> getQuestions(
            @RequestParam String topic,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(quizService.getQuestions(topic, limit));
    }

    // Get AI generated questions (fresh every time)
    @GetMapping("/questions/ai")
    public ResponseEntity<List<QuestionDto>> getAiQuestions(
            @RequestParam String topic,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(
                quizService.getAiGeneratedQuestions(topic, limit));
    }

    // Submit answers and get scored result
    @PostMapping("/submit")
    public ResponseEntity<QuizResultDto> submitQuiz(
            @RequestBody QuizSubmitRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }

    // Get all quiz history
    @GetMapping("/history")
    public ResponseEntity<List<QuizResultDto>> getMyHistory() {
        return ResponseEntity.ok(quizService.getMyHistory());
    }

    // Get quiz history by topic
    @GetMapping("/history/topic")
    public ResponseEntity<List<QuizResultDto>> getHistoryByTopic(
            @RequestParam String topic) {
        return ResponseEntity.ok(quizService.getHistoryByTopic(topic));
    }
}