package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.dto.*;
import com.eduai.eduai_backend.entity.*;
import com.eduai.eduai_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    // ── Helper ────────────────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Get available topics ──────────────────────────────
    public List<String> getTopics() {
        return List.of("Java", "DSA", "SQL", "Spring Boot", "System Design","AI/ML");
    }

    // ── Get fixed DB questions ────────────────────────────
    public List<QuestionDto> getQuestions(String topic, int limit) {
        List<Question> questions = questionRepository
                .findRandomByTopic(topic, limit);
        return questions.stream()
                .map(q -> QuestionDto.builder()
                        .id(q.getId())
                        .topic(q.getTopic())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .difficulty(q.getDifficulty())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Get AI generated questions (dynamic) ─────────────
    public List<QuestionDto> getAiGeneratedQuestions(String topic, int limit) {
        String prompt = """
                Generate %d multiple choice questions about %s for a software engineering student.
                
                Return ONLY a JSON array in this exact format, no extra text, no markdown:
                [
                  {
                    "questionText": "What is...?",
                    "optionA": "First option",
                    "optionB": "Second option",
                    "optionC": "Third option",
                    "optionD": "Fourth option",
                    "correctAnswer": "A",
                    "explanation": "Because...",
                    "difficulty": "Medium"
                  }
                ]
                """.formatted(limit, topic);

        String aiResponse = geminiService.generateResponse(prompt);

        try {
            // Clean response — remove markdown code blocks if present
            String cleaned = aiResponse
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            // Find JSON array start and end
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start != -1 && end != -1) {
                cleaned = cleaned.substring(start, end + 1);
            }

            org.json.JSONArray jsonArray = new org.json.JSONArray(cleaned);
            List<QuestionDto> questions = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject obj = jsonArray.getJSONObject(i);
                questions.add(QuestionDto.builder()
                        .id((long) (i + 1))
                        .topic(topic)
                        .questionText(obj.getString("questionText"))
                        .optionA(obj.getString("optionA"))
                        .optionB(obj.getString("optionB"))
                        .optionC(obj.getString("optionC"))
                        .optionD(obj.getString("optionD"))
                        .difficulty(obj.optString("difficulty", "Medium"))
                        .build());
            }

            System.out.println("AI generated " + questions.size()
                    + " questions for topic: " + topic);
            return questions;

        } catch (Exception e) {
            System.err.println("AI question parse error: " + e.getMessage()
                    + " — falling back to DB questions");
            return getQuestions(topic, limit);
        }
    }

    // ── Submit quiz and get scored result ─────────────────
    public QuizResultDto submitQuiz(QuizSubmitRequest request) {
        User user = getCurrentUser();
        Map<Long, String> answers = request.getAnswers();

        List<QuizResultDto.QuestionFeedback> feedbackList = new ArrayList<>();
        int correct = 0;

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String studentAnswer = entry.getValue();

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException(
                            "Question not found: " + questionId));

            boolean isCorrect = question.getCorrectAnswer()
                    .equalsIgnoreCase(studentAnswer);

            if (isCorrect) correct++;

            // AI explanation — from DB for correct, from Groq for wrong
            String aiExplanation = question.getExplanation();
            if (!isCorrect) {
                String prompt = "Question: " + question.getQuestionText()
                        + "\nCorrect answer: " + question.getCorrectAnswer()
                        + " (" + getOptionText(question,
                        question.getCorrectAnswer()) + ")"
                        + "\nStudent answered: " + studentAnswer
                        + " (" + getOptionText(question, studentAnswer) + ")"
                        + "\nGive a brief 2-3 line explanation of why the"
                        + " correct answer is right.";
                aiExplanation = geminiService.generateResponse(prompt);
            }

            feedbackList.add(QuizResultDto.QuestionFeedback.builder()
                    .questionId(questionId)
                    .questionText(question.getQuestionText())
                    .yourAnswer(studentAnswer)
                    .correctAnswer(question.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .aiExplanation(aiExplanation)
                    .build());
        }

        int total = answers.size();
        double percentage = total > 0 ? (correct * 100.0) / total : 0;
        String grade = calculateGrade(percentage);

        // Save result to DB
        QuizResult result = QuizResult.builder()
                .user(user)
                .topic(request.getTopic())
                .totalQuestions(total)
                .correctAnswers(correct)
                .scorePercentage(percentage)
                .build();
        quizResultRepository.save(result);

        return QuizResultDto.builder()
                .id(result.getId())
                .topic(request.getTopic())
                .totalQuestions(total)
                .correctAnswers(correct)
                .scorePercentage(percentage)
                .grade(grade)
                .feedback(feedbackList)
                .takenAt(result.getTakenAt())
                .build();
    }

    // ── Get quiz history ──────────────────────────────────
    public List<QuizResultDto> getMyHistory() {
        User user = getCurrentUser();
        return quizResultRepository
                .findByUserOrderByTakenAtDesc(user)
                .stream()
                .map(r -> QuizResultDto.builder()
                        .id(r.getId())
                        .topic(r.getTopic())
                        .totalQuestions(r.getTotalQuestions())
                        .correctAnswers(r.getCorrectAnswers())
                        .scorePercentage(r.getScorePercentage())
                        .grade(calculateGrade(r.getScorePercentage()))
                        .takenAt(r.getTakenAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Get history by topic ──────────────────────────────
    public List<QuizResultDto> getHistoryByTopic(String topic) {
        User user = getCurrentUser();
        return quizResultRepository
                .findByUserAndTopicOrderByTakenAtDesc(user, topic)
                .stream()
                .map(r -> QuizResultDto.builder()
                        .id(r.getId())
                        .topic(r.getTopic())
                        .totalQuestions(r.getTotalQuestions())
                        .correctAnswers(r.getCorrectAnswers())
                        .scorePercentage(r.getScorePercentage())
                        .grade(calculateGrade(r.getScorePercentage()))
                        .takenAt(r.getTakenAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────
    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        return "F";
    }

    private String getOptionText(Question q, String option) {
        return switch (option.toUpperCase()) {
            case "A" -> q.getOptionA();
            case "B" -> q.getOptionB();
            case "C" -> q.getOptionC();
            case "D" -> q.getOptionD();
            default -> "Unknown";
        };
    }
}