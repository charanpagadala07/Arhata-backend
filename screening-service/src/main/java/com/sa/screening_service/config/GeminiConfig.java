package com.sa.screening_service.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public Client geminiClient(@Value("${gemini.api-key:}") String apiKey) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        return new Client.Builder()
                .apiKey(apiKey)
                .build();
    }
}