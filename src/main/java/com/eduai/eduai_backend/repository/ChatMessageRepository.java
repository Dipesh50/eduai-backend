package com.eduai.eduai_backend.repository;


import com.eduai.eduai_backend.entity.ChatMessage;
import com.eduai.eduai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByCreatedAtAsc(User user);
    List<ChatMessage> findTop10ByUserOrderByCreatedAtDesc(User user);
}