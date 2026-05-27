package com.eduai.eduai_backend.controller;


import com.eduai.eduai_backend.dto.SkillGapReportDto;
import com.eduai.eduai_backend.service.SkillGapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skillgap")
@RequiredArgsConstructor
public class SkillGapController {

    private final SkillGapService skillGapService;

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getTargetRoles() {
        return ResponseEntity.ok(skillGapService.getTargetRoles());
    }

    @PostMapping("/analyze")
    public ResponseEntity<SkillGapReportDto> analyzeSkillGap(
            @RequestParam String targetRole) {
        return ResponseEntity.ok(
                skillGapService.analyzeSkillGap(targetRole));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SkillGapReportDto>> getHistory() {
        return ResponseEntity.ok(skillGapService.getMyHistory());
    }
}