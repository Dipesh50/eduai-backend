package com.eduai.eduai_backend.controller;


import com.eduai.eduai_backend.dto.NoteDto;
import com.eduai.eduai_backend.dto.NoteRequest;
import com.eduai.eduai_backend.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteDto> createNote(
            @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.status(201)
                .body(noteService.createNote(request));
    }

    @GetMapping
    public ResponseEntity<List<NoteDto>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @GetMapping("/topic")
    public ResponseEntity<List<NoteDto>> getNotesByTopic(
            @RequestParam String topic) {
        return ResponseEntity.ok(
                noteService.getNotesByTopic(topic));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteDto>> searchNotes(
            @RequestParam String keyword) {
        return ResponseEntity.ok(
                noteService.searchNotes(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.ok(
                noteService.updateNote(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}