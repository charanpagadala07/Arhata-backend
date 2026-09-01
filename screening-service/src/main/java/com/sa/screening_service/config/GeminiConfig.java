package com.sa.screening_service.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public Client geminiClient() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        System.out.println("GOOGLE_API_KEY present: " + (apiKey != null));
        System.out.println("GOOGLE_API_KEY length: " +
                (apiKey != null ? apiKey.length() : 0));

        return new Client.Builder()
                .apiKey(apiKey)
                .build();
    }
}