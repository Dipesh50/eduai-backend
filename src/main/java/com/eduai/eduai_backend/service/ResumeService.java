package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.dto.ResumeAnalysisDto;
import com.eduai.eduai_backend.entity.ResumeAnalysis;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.ResumeAnalysisRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    // ── Predefined skill sets by role ─────────────────────
    private static final Map<String, List<String>> ROLE_SKILLS =
            new LinkedHashMap<>();

    static {
        ROLE_SKILLS.put("Java Backend Developer", List.of(
                "java", "spring boot", "spring", "hibernate", "jpa",
                "rest api", "restful", "mysql", "sql", "jdbc",
                "maven", "gradle", "git", "junit", "microservices",
                "docker", "aws", "redis", "kafka", "multithreading"
        ));

        ROLE_SKILLS.put("Full Stack Developer", List.of(
                "java", "spring boot", "react", "angular", "javascript",
                "html", "css", "mysql", "mongodb", "rest api",
                "git", "docker", "node.js", "typescript", "redux",
                "tailwind", "bootstrap", "aws", "ci/cd", "postman"
        ));

        ROLE_SKILLS.put("Data Structures & Algorithms", List.of(
                "array", "linked list", "stack", "queue", "tree",
                "graph", "dynamic programming", "recursion", "sorting",
                "searching", "binary search", "hashing", "heap",
                "greedy", "backtracking", "complexity", "leetcode"
        ));

        ROLE_SKILLS.put("DevOps Engineer", List.of(
                "docker", "kubernetes", "jenkins", "git", "linux",
                "aws", "azure", "gcp", "terraform", "ansible",
                "ci/cd", "bash", "python", "monitoring", "nginx",
                "prometheus", "grafana", "helm", "maven", "gradle"
        ));

        ROLE_SKILLS.put("Android Developer", List.of(
                "android", "kotlin", "java", "xml", "jetpack compose",
                "retrofit", "room", "mvvm", "firebase", "git",
                "rest api", "sqlite", "coroutines", "viewmodel",
                "livedata", "navigation", "material design", "gradle"
        ));
    }

    // ── Helper ────────────────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Main analyze method ───────────────────────────────
    public ResumeAnalysisDto analyzeResume(MultipartFile file) {
        User user = getCurrentUser();

        // Step 1 — Extract text from PDF
        String extractedText = extractTextFromPdf(file);
        if (extractedText.isEmpty()) {
            throw new RuntimeException(
                    "Could not extract text from PDF. " +
                            "Make sure the PDF has selectable text.");
        }

        System.out.println("Extracted text length: "
                + extractedText.length());

        // Step 2 — Calculate real ATS score by keyword matching
        AtsResult atsResult = calculateAtsScore(extractedText);
        System.out.println("Best role: " + atsResult.bestRole
                + " | ATS Score: " + atsResult.atsScore);

        // Step 3 — Build AI prompt with real scores
        String prompt = buildAnalysisPrompt(
                extractedText, atsResult);

        // Step 4 — Call Groq AI for strengths and suggestions
        String aiResponse = geminiService.generateResponse(prompt);
        System.out.println("AI Response: " + aiResponse);

        // Step 5 — Parse AI response
        ResumeAnalysisDto dto = parseAiResponse(
                aiResponse, atsResult);

        // Step 6 — Save to DB
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .extractedText(extractedText.substring(
                        0, Math.min(extractedText.length(), 5000)))
                .atsScore(dto.getAtsScore())
                .presentSkills(String.join(",", dto.getPresentSkills()))
                .missingSkills(String.join(",", dto.getMissingSkills()))
                .suggestions(String.join("||", dto.getSuggestions()))
                .strengths(String.join("||", dto.getStrengths()))
                .overallGrade(dto.getOverallGrade())
                .build();

        resumeAnalysisRepository.save(analysis);

        dto.setId(analysis.getId());
        dto.setFileName(file.getOriginalFilename());
        dto.setAnalyzedAt(analysis.getAnalyzedAt());

        return dto;
    }

    // ── Get analysis history ──────────────────────────────
    public List<ResumeAnalysisDto> getMyHistory() {
        User user = getCurrentUser();
        List<ResumeAnalysisDto> result = new ArrayList<>();

        resumeAnalysisRepository
                .findByUserOrderByAnalyzedAtDesc(user)
                .forEach(a -> result.add(
                        ResumeAnalysisDto.builder()
                                .id(a.getId())
                                .fileName(a.getFileName())
                                .atsScore(a.getAtsScore())
                                .overallGrade(a.getOverallGrade())
                                .presentSkills(splitToList(
                                        a.getPresentSkills(), ","))
                                .missingSkills(splitToList(
                                        a.getMissingSkills(), ","))
                                .suggestions(splitToList(
                                        a.getSuggestions(), "\\|\\|"))
                                .strengths(splitToList(
                                        a.getStrengths(), "\\|\\|"))
                                .analyzedAt(a.getAnalyzedAt())
                                .build()));
        return result;
    }

    // ── Calculate ATS score by keyword matching ───────────
    private AtsResult calculateAtsScore(String resumeText) {
        String lowerResume = resumeText.toLowerCase();

        String bestRole = "";
        int bestScore = 0;
        List<String> bestPresent = new ArrayList<>();
        List<String> bestMissing = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry :
                ROLE_SKILLS.entrySet()) {

            String role = entry.getKey();
            List<String> skills = entry.getValue();

            List<String> present = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            for (String skill : skills) {
                if (lowerResume.contains(skill.toLowerCase())) {
                    present.add(skill);
                } else {
                    missing.add(skill);
                }
            }

            int score = (int) Math.round(
                    (present.size() * 100.0) / skills.size());

            if (score > bestScore) {
                bestScore = score;
                bestRole = role;
                bestPresent = present;
                bestMissing = missing;
            }
        }

        return new AtsResult(bestRole, bestScore,
                bestPresent, bestMissing);
    }

    // ── Build AI prompt with real ATS data ────────────────
    private String buildAnalysisPrompt(
            String resumeText, AtsResult ats) {

        String truncated = resumeText.substring(
                0, Math.min(resumeText.length(), 3000));

        return """
                Analyze this software engineering student resume.

                Resume text:
                %s

                Keyword analysis already calculated:
                Best matching role: %s
                ATS Score (keyword-based): %d out of 100
                Skills found in resume: %s
                Skills missing from resume: %s

                Based on the above data, return ONLY a valid JSON
                object (no markdown, no code blocks, no extra text):
                {
                  "atsScore": %d,
                  "bestRole": "%s",
                  "overallGrade": "%s",
                  "presentSkills": %s,
                  "missingSkills": %s,
                  "strengths": [
                    "Write 2 to 3 specific strengths from the resume"
                  ],
                  "suggestions": [
                    "Write 3 to 4 specific actionable improvement tips"
                  ]
                }

                Grade rules:
                90 and above = A+
                80 to 89 = A
                70 to 79 = B+
                60 to 69 = B
                50 to 59 = C
                below 50 = F

                Only return the JSON object. Nothing else.
                """.formatted(
                truncated,
                ats.bestRole,
                ats.atsScore,
                ats.presentSkills.toString(),
                ats.missingSkills.toString(),
                ats.atsScore,
                ats.bestRole,
                calculateGrade(ats.atsScore),
                new JSONArray(ats.presentSkills),
                new JSONArray(ats.missingSkills)
        );
    }

    // ── Parse AI JSON response ────────────────────────────
    private ResumeAnalysisDto parseAiResponse(
            String aiResponse, AtsResult ats) {
        try {
            String cleaned = aiResponse
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start != -1 && end != -1) {
                cleaned = cleaned.substring(start, end + 1);
            }

            JSONObject json = new JSONObject(cleaned);

            return ResumeAnalysisDto.builder()
                    .atsScore(ats.atsScore)
                    .overallGrade(calculateGrade(ats.atsScore))
                    .presentSkills(ats.presentSkills)
                    .missingSkills(ats.missingSkills)
                    .strengths(jsonArrayToList(
                            json.optJSONArray("strengths")))
                    .suggestions(jsonArrayToList(
                            json.optJSONArray("suggestions")))
                    .build();

        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            System.err.println("Raw response: " + aiResponse);

            return ResumeAnalysisDto.builder()
                    .atsScore(ats.atsScore)
                    .overallGrade(calculateGrade(ats.atsScore))
                    .presentSkills(ats.presentSkills)
                    .missingSkills(ats.missingSkills)
                    .strengths(new ArrayList<>())
                    .suggestions(List.of(
                            "Could not generate AI suggestions.",
                            "Please try again."))
                    .build();
        }
    }

    // ── Extract text from PDF using PDFBox 3.x ────────────
    private String extractTextFromPdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document).trim();
                System.out.println("PDF extracted successfully.");
                return text;
            }
        } catch (Exception e) {
            System.err.println("PDF extraction error: "
                    + e.getMessage());
            return "";
        }
    }

    // ── Grade calculator ──────────────────────────────────
    private String calculateGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        return "F";
    }

    // ── Helpers ───────────────────────────────────────────
    private List<String> jsonArrayToList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    private List<String> splitToList(
            String value, String delimiter) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(value.split(delimiter)));
    }

    // ── Inner class for ATS result ────────────────────────
    private static class AtsResult {
        String bestRole;
        int atsScore;
        List<String> presentSkills;
        List<String> missingSkills;

        AtsResult(String bestRole, int atsScore,
                  List<String> presentSkills,
                  List<String> missingSkills) {
            this.bestRole = bestRole;
            this.atsScore = atsScore;
            this.presentSkills = presentSkills;
            this.missingSkills = missingSkills;
        }
    }
}
