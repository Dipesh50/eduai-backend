package com.eduai.eduai_backend.service;


import com.eduai.eduai_backend.dto.ChatRequest;
import com.eduai.eduai_backend.dto.ChatResponse;
import com.eduai.eduai_backend.entity.ChatMessage;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.ChatMessageRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Send message and get AI response
    public ChatResponse sendMessage(ChatRequest request) {
        User user = getCurrentUser();

        // Call Gemini API
        String aiResponse = geminiService.generateResponse(request.getMessage());

        // Save both messages to DB
        ChatMessage chatMessage = ChatMessage.builder()
                .user(user)
                .userMessage(request.getMessage())
                .aiResponse(aiResponse)
                .build();

        chatMessageRepository.save(chatMessage);

        return ChatResponse.builder()
                .id(chatMessage.getId())
                .userMessage(chatMessage.getUserMessage())
                .aiResponse(aiResponse)
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }

    // Get full chat history
    public List<ChatResponse> getChatHistory() {
        User user = getCurrentUser();
        return chatMessageRepository
                .findByUserOrderByCreatedAtAsc(user)
                .stream()
                .map(msg -> ChatResponse.builder()
                        .id(msg.getId())
                        .userMessage(msg.getUserMessage())
                        .aiResponse(msg.getAiResponse())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Get last 10 messages only
    public List<ChatResponse> getRecentHistory() {
        User user = getCurrentUser();
        return chatMessageRepository
                .findTop10ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(msg -> ChatResponse.builder()
                        .id(msg.getId())
                        .userMessage(msg.getUserMessage())
                        .aiResponse(msg.getAiResponse())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}