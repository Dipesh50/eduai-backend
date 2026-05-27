package com.eduai.eduai_backend.service;


import com.eduai.eduai_backend.dto.NoteDto;
import com.eduai.eduai_backend.dto.NoteRequest;
import com.eduai.eduai_backend.entity.Note;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.NoteRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Create note ───────────────────────────────────────
    public NoteDto createNote(NoteRequest request) {
        User user = getCurrentUser();

        Note note = Note.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .topic(request.getTopic())
                .tag(request.getTag())
                .build();

        noteRepository.save(note);
        return toDto(note);
    }

    // ── Get all notes ─────────────────────────────────────
    public List<NoteDto> getAllNotes() {
        User user = getCurrentUser();
        return noteRepository
                .findByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Get notes by topic ────────────────────────────────
    public List<NoteDto> getNotesByTopic(String topic) {
        User user = getCurrentUser();
        return noteRepository
                .findByUserAndTopicOrderByUpdatedAtDesc(user, topic)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Search notes ──────────────────────────────────────
    public List<NoteDto> searchNotes(String keyword) {
        User user = getCurrentUser();
        return noteRepository
                .findByUserAndTitleContainingIgnoreCaseOrUserAndContentContainingIgnoreCase(
                        user, keyword, user, keyword)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Update note ───────────────────────────────────────
    public NoteDto updateNote(Long id, NoteRequest request) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException(
                        "Note not found"));

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTopic(request.getTopic());
        note.setTag(request.getTag());
        noteRepository.save(note);
        return toDto(note);
    }

    // ── Delete note ───────────────────────────────────────
    public void deleteNote(Long id) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException(
                        "Note not found"));
        noteRepository.delete(note);
    }

    private NoteDto toDto(Note note) {
        return NoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .topic(note.getTopic())
                .tag(note.getTag())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}