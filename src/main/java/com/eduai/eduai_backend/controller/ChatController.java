package com.eduai.eduai_backend.controller;

import com.eduai.eduai_backend.dto.ChatRequest;
import com.eduai.eduai_backend.dto.ChatResponse;
import com.eduai.eduai_backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatResponse>> getChatHistory() {
        return ResponseEntity.ok(chatService.getChatHistory());
    }

    @GetMapping("/history/recent")
    public ResponseEntity<List<ChatResponse>> getRecentHistory() {
        return ResponseEntity.ok(chatService.getRecentHistory());
    }
}