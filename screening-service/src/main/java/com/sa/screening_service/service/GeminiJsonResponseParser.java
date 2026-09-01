package com.sa.screening_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sa.screening_service.dto.ScreeningResponse;
import org.springframework.stereotype.Component;

@Component
public class GeminiJsonResponseParser implements GeminiResponseParser {

    private final ObjectMapper objectMapper;

    public GeminiJsonResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ScreeningResponse parse(String rawResponse) {

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException(
                    "Gemini returned an empty response"
            );
        }

        String cleanedResponse = cleanMarkdown(rawResponse);

        try {

            ScreeningResponse response =
                    objectMapper.readValue(
                            cleanedResponse,
                            ScreeningResponse.class
                    );

            validateResponse(response);

            return response;

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Unable to parse Gemini response: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private String cleanMarkdown(String response) {

        String cleaned = response.trim();

        /*
         * Gemini sometimes returns:
         *
         * ```json
         * {...}
         * ```
         *
         * Jackson expects pure JSON.
         */

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(
                    0,
                    cleaned.length() - 3
            ).trim();
        }

        return cleaned;
    }

    private void validateResponse(ScreeningResponse response) {

        if (response == null) {
            throw new IllegalArgumentException(
                    "Gemini returned null response"
            );
        }

        if (response.getCandidates() == null) {
            throw new IllegalArgumentException(
                    "Gemini response does not contain candidates"
            );
        }
    }
}