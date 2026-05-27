package com.eduai.eduai_backend.service;

import com.eduai.eduai_backend.dto.*;
import com.eduai.eduai_backend.entity.MockInterview;
import com.eduai.eduai_backend.entity.User;
import com.eduai.eduai_backend.repository.MockInterviewRepository;
import com.eduai.eduai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    private static final int MAX_QUESTIONS = 5;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Start a new interview ─────────────────────────────
    public InterviewResponseDto startInterview(
            InterviewStartRequest request) {

        User user = getCurrentUser();

        // Build conversation history as JSON array
        JSONArray history = new JSONArray();

        // Generate first question
        String firstQuestion = generateFirstQuestion(
                request.getTopic(), history);

        // Add AI question to history
        history.put(new JSONObject()
                .put("role", "ai")
                .put("content", firstQuestion));

        // Save interview session
        MockInterview interview = MockInterview.builder()
                .user(user)
                .topic(request.getTopic())
                .conversationHistory(history.toString())
                .questionCount(1)
                .completed(false)
                .build();

        mockInterviewRepository.save(interview);

        return InterviewResponseDto.builder()
                .interviewId(interview.getId())
                .question(firstQuestion)
                .questionNumber(1)
                .isCompleted(false)
                .build();
    }

    // ── Submit answer and get next question ───────────────
    public InterviewResponseDto submitAnswer(
            Long interviewId, InterviewAnswerRequest request) {

        User user = getCurrentUser();
        MockInterview interview = mockInterviewRepository
                .findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new RuntimeException(
                        "Interview not found"));

        if (interview.isCompleted()) {
            throw new RuntimeException(
                    "This interview is already completed");
        }

        // Load conversation history
        JSONArray history = new JSONArray(
                interview.getConversationHistory());

        // Add student answer to history
        history.put(new JSONObject()
                .put("role", "student")
                .put("content", request.getAnswer()));

        int nextQuestionNum = interview.getQuestionCount() + 1;

        // Check if interview is complete
        if (interview.getQuestionCount() >= MAX_QUESTIONS) {

            // Generate final feedback
            InterviewFeedbackDto feedback =
                    generateFeedback(interview.getTopic(), history);

            // Save completed interview
            interview.setConversationHistory(history.toString());
            interview.setCompleted(true);
            interview.setCompletedAt(LocalDateTime.now());
            interview.setConfidenceScore(feedback.getConfidenceScore());
            interview.setFeedbackReport(feedback.getFeedbackReport());
            interview.setWeakAreas(
                    String.join("||", feedback.getWeakAreas()));
            interview.setStrongPoints(
                    String.join("||", feedback.getStrongPoints()));
            mockInterviewRepository.save(interview);

            return InterviewResponseDto.builder()
                    .interviewId(interviewId)
                    .questionNumber(interview.getQuestionCount())
                    .isCompleted(true)
                    .feedback(feedback)
                    .build();
        }

        // Generate follow-up question based on answer
        String nextQuestion = generateFollowUpQuestion(
                interview.getTopic(), history, nextQuestionNum);

        // Add AI follow-up to history
        history.put(new JSONObject()
                .put("role", "ai")
                .put("content", nextQuestion));

        // Update interview
        interview.setConversationHistory(history.toString());
        interview.setQuestionCount(nextQuestionNum);
        mockInterviewRepository.save(interview);

        return InterviewResponseDto.builder()
                .interviewId(interviewId)
                .question(nextQuestion)
                .questionNumber(nextQuestionNum)
                .isCompleted(false)
                .build();
    }

    // ── Get interview history ─────────────────────────────
    public List<InterviewFeedbackDto> getMyHistory() {
        User user = getCurrentUser();
        List<InterviewFeedbackDto> result = new ArrayList<>();

        mockInterviewRepository
                .findByUserOrderByStartedAtDesc(user)
                .stream()
                .filter(MockInterview::isCompleted)
                .forEach(i -> result.add(
                        InterviewFeedbackDto.builder()
                                .interviewId(i.getId())
                                .topic(i.getTopic())
                                .confidenceScore(i.getConfidenceScore())
                                .overallGrade(calculateGrade(
                                        i.getConfidenceScore()))
                                .feedbackReport(i.getFeedbackReport())
                                .strongPoints(splitToList(
                                        i.getStrongPoints()))
                                .weakAreas(splitToList(
                                        i.getWeakAreas()))
                                .build()));
        return result;
    }

    // ── Generate first question ───────────────────────────
    private String generateFirstQuestion(
            String topic, JSONArray history) {

        String prompt = """
                You are an experienced technical interviewer at a top
                software company. Start a mock interview for a student
                on the topic: %s

                Ask ONE clear, specific technical question suitable for
                a student applying for internships. Start with a
                medium-difficulty question. Only return the question,
                nothing else.
                """.formatted(topic);

        return geminiService.generateResponse(prompt);
    }

    // ── Generate follow-up question ───────────────────────
    private String generateFollowUpQuestion(
            String topic, JSONArray history, int questionNum) {

        StringBuilder conversation = new StringBuilder();
        for (int i = 0; i < history.length(); i++) {
            JSONObject msg = history.getJSONObject(i);
            String role = msg.getString("role")
                    .equals("ai") ? "Interviewer" : "Student";
            conversation.append(role).append(": ")
                    .append(msg.getString("content"))
                    .append("\n\n");
        }

        String prompt = """
                You are a technical interviewer. This is question %d
                of %d in a %s interview.

                Conversation so far:
                %s

                Based on the student's last answer, ask ONE follow-up
                question. If the answer was weak, probe deeper on that
                weakness. If strong, move to a related harder concept.
                Only return the question, nothing else.
                """.formatted(questionNum, MAX_QUESTIONS,
                topic, conversation.toString());

        return geminiService.generateResponse(prompt);
    }

    // ── Generate final feedback report ───────────────────
    private InterviewFeedbackDto generateFeedback(
            String topic, JSONArray history) {

        StringBuilder conversation = new StringBuilder();
        for (int i = 0; i < history.length(); i++) {
            JSONObject msg = history.getJSONObject(i);
            String role = msg.getString("role")
                    .equals("ai") ? "Interviewer" : "Student";
            conversation.append(role).append(": ")
                    .append(msg.getString("content"))
                    .append("\n\n");
        }

        String prompt = """
                You are a senior technical interviewer. Evaluate this
                complete mock interview on %s topic.

                Full interview:
                %s

                Return ONLY a valid JSON object (no markdown):
                {
                  "confidenceScore": 75,
                  "feedbackReport": "Overall summary in 2-3 sentences",
                  "strongPoints": [
                    "Good understanding of X",
                    "Clear explanation of Y"
                  ],
                  "weakAreas": [
                    "Needs improvement in Z",
                    "Should study more about W"
                  ],
                  "improvementTips": [
                    "Practice more problems on X",
                    "Read about Y concept"
                  ]
                }

                Confidence score rules:
                90-100 = Excellent, ready for interviews
                70-89 = Good, minor preparation needed
                50-69 = Average, needs more practice
                Below 50 = Needs significant preparation

                Only return JSON, nothing else.
                """.formatted(topic, conversation.toString());

        String aiResponse = geminiService.generateResponse(prompt);

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
            int score = json.optInt("confidenceScore", 60);

            return InterviewFeedbackDto.builder()
                    .topic(topic)
                    .confidenceScore(score)
                    .overallGrade(calculateGrade(score))
                    .feedbackReport(json.optString(
                            "feedbackReport", "Interview completed."))
                    .strongPoints(jsonArrayToList(
                            json.optJSONArray("strongPoints")))
                    .weakAreas(jsonArrayToList(
                            json.optJSONArray("weakAreas")))
                    .improvementTips(jsonArrayToList(
                            json.optJSONArray("improvementTips")))
                    .build();

        } catch (Exception e) {
            System.err.println("Feedback parse error: "
                    + e.getMessage());
            return InterviewFeedbackDto.builder()
                    .topic(topic)
                    .confidenceScore(60)
                    .overallGrade("C")
                    .feedbackReport("Interview completed.")
                    .strongPoints(new ArrayList<>())
                    .weakAreas(new ArrayList<>())
                    .improvementTips(new ArrayList<>())
                    .build();
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

    private List<String> jsonArrayToList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    private List<String> splitToList(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(value.split("\\|\\|")));
    }
}