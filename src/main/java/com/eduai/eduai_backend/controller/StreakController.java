package com.eduai.eduai_backend.controller;

import com.eduai.eduai_backend.dto.StreakDto;
import com.eduai.eduai_backend.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streak")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    @PostMapping("/checkin")
    public ResponseEntity<StreakDto> checkIn() {
        return ResponseEntity.ok(streakService.checkIn());
    }

    @GetMapping("/status")
    public ResponseEntity<StreakDto> getStatus() {
        return ResponseEntity.ok(streakService.getStatus());
    }
}