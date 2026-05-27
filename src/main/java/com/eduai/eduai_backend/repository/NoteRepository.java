package com.eduai.eduai_backend.repository;

import com.eduai.eduai_backend.entity.Note;
import com.eduai.eduai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteRepository
        extends JpaRepository<Note, Long> {

    List<Note> findByUserOrderByUpdatedAtDesc(User user);

    List<Note> findByUserAndTopicOrderByUpdatedAtDesc(
            User user, String topic);

    List<Note> findByUserAndTitleContainingIgnoreCaseOrUserAndContentContainingIgnoreCase(
            User user, String title, User user2, String content);

    Optional<Note> findByIdAndUser(Long id, User user);

    long countByUser(User user);
}