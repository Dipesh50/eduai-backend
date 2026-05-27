package com.eduai.eduai_backend.service;


import com.eduai.eduai_backend.dto.SkillGapReportDto;
import com.eduai.eduai_backend.entity.SkillGapReport;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.QuizResultRepository;
import com.eduai.eduai_backend.repository.ResumeAnalysisRepository;
import com.eduai.eduai_backend.repository.SkillGapRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SkillGapService {

    private final SkillGapRepository skillGapRepository;
    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final GeminiService geminiService;

    // ── Required skills per role ──────────────────────────
    private static final Map<String, List<String>> ROLE_REQUIREMENTS
            = new LinkedHashMap<>();

    static {
        ROLE_REQUIREMENTS.put("Java Backend Developer at MNC", List.of(
                "Java", "Spring Boot", "Spring Security", "JPA",
                "Hibernate", "MySQL", "REST API", "Microservices",
                "Docker", "Git", "Maven", "JUnit", "Redis", "Kafka"
        ));
        ROLE_REQUIREMENTS.put("Full Stack Developer", List.of(
                "Java", "Spring Boot", "React", "JavaScript",
                "HTML", "CSS", "MySQL", "REST API", "Git",
                "Docker", "TypeScript", "MongoDB", "Redux"
        ));
        ROLE_REQUIREMENTS.put("Android Developer", List.of(
                "Kotlin", "Java", "Android SDK", "Jetpack Compose",
                "MVVM", "Retrofit", "Room DB", "Firebase",
                "Git", "REST API", "Material Design", "Coroutines"
        ));
        ROLE_REQUIREMENTS.put("DevOps Engineer", List.of(
                "Docker", "Kubernetes", "Jenkins", "Linux",
                "AWS", "Terraform", "Git", "CI/CD", "Bash",
                "Python", "Nginx", "Prometheus", "Grafana"
        ));
        ROLE_REQUIREMENTS.put("Data Engineer", List.of(
                "Python", "SQL", "Spark", "Hadoop", "Kafka",
                "Airflow", "AWS", "ETL", "Data Modeling",
                "MongoDB", "PostgreSQL", "Git", "Docker"
        ));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Get available target roles ────────────────────────
    public List<String> getTargetRoles() {
        return new ArrayList<>(ROLE_REQUIREMENTS.keySet());
    }

    // ── Analyze skill gap for target role ─────────────────
    public SkillGapReportDto analyzeSkillGap(String targetRole) {
        User user = getCurrentUser();

        List<String> requiredSkills = ROLE_REQUIREMENTS
                .getOrDefault(targetRole,
                        ROLE_REQUIREMENTS.get(
                                "Java Backend Developer at MNC"));

        // ── Collect student's current skills ──────────────

        // From latest resume analysis
        Set<String> studentSkills = new HashSet<>();
        resumeAnalysisRepository
                .findTopByUserOrderByAnalyzedAtDesc(user)
                .ifPresent(r -> {
                    if (r.getPresentSkills() != null) {
                        Arrays.stream(r.getPresentSkills().split(","))
                                .map(String::trim)
                                .forEach(studentSkills::add);
                    }
                });

        // From quiz performance — add topics with good scores
        quizResultRepository.findByUserOrderByTakenAtDesc(user)
                .stream()
                .filter(r -> r.getScorePercentage() >= 60)
                .forEach(r -> studentSkills.add(r.getTopic()));

        // ── Calculate readiness score ─────────────────────
        List<String> presentSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String skill : requiredSkills) {
            boolean found = studentSkills.stream()
                    .anyMatch(s -> s.toLowerCase()
                            .contains(skill.toLowerCase())
                            || skill.toLowerCase()
                            .contains(s.toLowerCase()));
            if (found) {
                presentSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        int readinessScore = (int) Math.round(
                (presentSkills.size() * 100.0) / requiredSkills.size());

        // ── Generate AI learning roadmap ──────────────────
        List<String> roadmap = generateLearningRoadmap(
                targetRole, missingSkills, readinessScore);

        // ── Save report ───────────────────────────────────
        SkillGapReport report = SkillGapReport.builder()
                .user(user)
                .targetRole(targetRole)
                .readinessScore(readinessScore)
                .presentSkills(String.join(",", presentSkills))
                .missingSkills(String.join(",", missingSkills))
                .learningRoadmap(String.join("||", roadmap))
                .build();

        skillGapRepository.save(report);

        return SkillGapReportDto.builder()
                .id(report.getId())
                .targetRole(targetRole)
                .readinessScore(readinessScore)
                .readinessGrade(calculateGrade(readinessScore))
                .presentSkills(presentSkills)
                .missingSkills(missingSkills)
                .learningRoadmap(roadmap)
                .createdAt(report.getCreatedAt())
                .build();
    }

    // ── Get skill gap history ─────────────────────────────
    public List<SkillGapReportDto> getMyHistory() {
        User user = getCurrentUser();
        List<SkillGapReportDto> result = new ArrayList<>();

        skillGapRepository.findByUserOrderByCreatedAtDesc(user)
                .forEach(r -> result.add(SkillGapReportDto.builder()
                        .id(r.getId())
                        .targetRole(r.getTargetRole())
                        .readinessScore(r.getReadinessScore())
                        .readinessGrade(calculateGrade(
                                r.getReadinessScore()))
                        .presentSkills(splitToList(
                                r.getPresentSkills(), ","))
                        .missingSkills(splitToList(
                                r.getMissingSkills(), ","))
                        .learningRoadmap(splitToList(
                                r.getLearningRoadmap(), "\\|\\|"))
                        .createdAt(r.getCreatedAt())
                        .build()));
        return result;
    }

    // ── Generate AI learning roadmap ──────────────────────
    private List<String> generateLearningRoadmap(
            String targetRole,
            List<String> missingSkills,
            int currentScore) {

        if (missingSkills.isEmpty()) {
            return List.of("You already have all required skills!",
                    "Focus on building projects and practicing DSA.");
        }

        String prompt = """
                A student wants to become a %s.
                Current readiness score: %d%%
                Missing skills: %s

                Create a prioritized learning roadmap.
                Return ONLY a JSON array of strings (no markdown):
                [
                  "Week 1-2: Learn X by doing Y",
                  "Week 3-4: Practice Z with project W",
                  "Week 5-6: Master A through B"
                ]

                Give 5-6 specific, actionable steps in priority order.
                Only return the JSON array, nothing else.
                """.formatted(
                targetRole,
                currentScore,
                missingSkills.toString());

        String aiResponse = geminiService.generateResponse(prompt);

        try {
            String cleaned = aiResponse
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start != -1 && end != -1) {
                cleaned = cleaned.substring(start, end + 1);
            }

            JSONArray array = new JSONArray(cleaned);
            List<String> roadmap = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                roadmap.add(array.getString(i));
            }
            return roadmap;

        } catch (Exception e) {
            System.err.println("Roadmap parse error: "
                    + e.getMessage());
            return List.of(
                    "Focus on learning: "
                            + String.join(", ", missingSkills));
        }
    }

    // ── Helpers ───────────────────────────────────────────
    private String calculateGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        return "F";
    }

    private List<String> splitToList(
            String value, String delimiter) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(value.split(delimiter)));
    }
}