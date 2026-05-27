package com.eduai.eduai_backend.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public String generateResponse(String userMessage) {
        try {
            JSONObject requestBody = new JSONObject()
                    .put("model", "llama-3.3-70b-versatile")
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "system")
                                    .put("content", "You are EduAI, an academic assistant helping students with programming, DSA, Java, Spring Boot, SQL, and interview preparation. Be clear and concise."))
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", userMessage)))
                    .put("max_tokens", 1024)
                    .put("temperature", 0.7);

            Request request = new Request.Builder()
                    .url(GROQ_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json; charset=utf-8")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                System.out.println(">>> Groq status: " + response.code());

                if (!response.isSuccessful()) {
                    return "API_ERROR_" + response.code() + ": " + responseBody;
                }

                JSONObject json = new JSONObject(responseBody);
                return json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }

        } catch (Exception e) {
            return "EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}