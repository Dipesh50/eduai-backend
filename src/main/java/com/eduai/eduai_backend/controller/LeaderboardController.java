package com.eduai.eduai_backend.controller;

import com.eduai.eduai_backend.dto.LeaderboardDto;
import com.eduai.eduai_backend.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<LeaderboardDto> getGlobalLeaderboard() {
        return ResponseEntity.ok(
                leaderboardService.getGlobalLeaderboard());
    }

    @GetMapping("/topic")
    public ResponseEntity<LeaderboardDto> getTopicLeaderboard(
            @RequestParam String topic) {
        return ResponseEntity.ok(
                leaderboardService.getTopicLeaderboard(topic));
    }
}