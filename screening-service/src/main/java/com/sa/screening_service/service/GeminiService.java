package com.sa.screening_service.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.sa.screening_service.dto.ScreeningResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client geminiClient;
    private final GeminiResponseParser responseParser;

    public GeminiService(
            Client geminiClient,
            GeminiResponseParser responseParser) {

        this.geminiClient = geminiClient;
        this.responseParser = responseParser;
    }

    public ScreeningResponse screenCandidates(
            byte[] excelBytes,
            String fileName,
            String jobDescription,
            String screeningCriteria) {

        if (excelBytes == null || excelBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Excel file cannot be empty"
            );
        }

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "Job description cannot be empty"
            );
        }

        if (screeningCriteria == null ||
                screeningCriteria.isBlank()) {

            throw new IllegalArgumentException(
                    "Screening criteria cannot be empty"
            );
        }

        String prompt = buildPrompt(
                fileName,
                jobDescription,
                screeningCriteria
        );

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .temperature(0.1f)
                        .build();

        /*
         * IMPORTANT:
         *
         * We are sending the REAL XLSX bytes.
         *
         * We are NOT converting the Excel into a String.
         * We are NOT extracting columns locally.
         */

        Part excelPart = Part.fromBytes(
                excelBytes,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        Part promptPart = Part.fromText(prompt);

        Content content = Content.fromParts(
                promptPart,
                excelPart
        );

        GenerateContentResponse response =
                geminiClient.models.generateContent(
                        "gemini-3.6-flash",
                        content,
                        config
                );

        String rawResponse = response.text();

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response"
            );
        }

        System.out.println("========== GEMINI RAW RESPONSE ==========");
        System.out.println(rawResponse);
        System.out.println("=========================================");

        return responseParser.parse(rawResponse);
    }

    private String buildPrompt(
            String fileName,
            String jobDescription,
            String screeningCriteria) {

        return """
                You are an AI recruitment screening engine.

                The attached file is the ORIGINAL candidate Excel
                spreadsheet.

                Analyze the spreadsheet directly.

                DO NOT assume that the application has extracted
                or preprocessed the candidate fields.

                Evaluate EVERY candidate in the spreadsheet.

                JOB DESCRIPTION:
                %s

                SCREENING CRITERIA:
                %s

                IMPORTANT SCREENING RULES:

                1. Evaluate every candidate.
                2. Consider all relevant fields present in the Excel.
                3. Consider technical skills and relevant experience.
                4. Consider current CTC and expected CTC.
                5. Consider notice period.
                6. Consider location.
                7. Consider education where relevant.
                8. Consider gender only if explicitly relevant to the
                   supplied screening criteria.
                9. Consider source and other candidate information
                   when relevant.
                10. Give every candidate a score from 0 to 100.
                11. Give every candidate a recommendation:
                    STRONG_MATCH
                    MODERATE_MATCH
                    LOW_MATCH
                12. Give a concise reason explaining the score.
                13. Return ALL candidates.
                14. Sort candidates from highest score to lowest score.

                RESPONSE FORMAT:

                Return ONLY valid JSON.

                Do NOT return Markdown.
                Do NOT use ```json.
                Do NOT use ```.

                Return exactly this structure:

                {
                  "candidates": [
                    {
                      "name": "string",
                      "email": "string",
                      "score": 0,
                      "recommendation": "STRONG_MATCH",
                      "reason": "string",
                      "linkedin": "string",
                      "portfolio": "string",
                      "github": "string",
                      "resume": "string"
                    }
                  ]
                }

                Uploaded file:
                %s
                """.formatted(
                jobDescription,
                screeningCriteria,
                fileName
        );
    }
}