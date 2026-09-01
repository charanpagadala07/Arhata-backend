package com.sa.screening_service.service;

import com.sa.screening_service.dto.CandidateResult;
import com.sa.screening_service.entity.ScreeningResult;
import org.springframework.stereotype.Component;

@Component
public class ScreeningResultMapper {

    public ScreeningResult toEntity(
            CandidateResult candidate,
            Long jobId,
            Integer rank) {

        return ScreeningResult.builder()
                .jobId(jobId)
                .rank(rank)
                .name(candidate.getName())
                .email(candidate.getEmail())
                .score(candidate.getScore())
                .recommendation(candidate.getRecommendation())
                .reason(candidate.getReason())
                .linkedin(candidate.getLinkedin())
                .portfolio(candidate.getPortfolio())
                .github(candidate.getGithub())
                .resume(candidate.getResume())
                .build();
    }
}