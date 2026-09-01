package com.sa.screening_service.service;

import com.sa.screening_service.dto.CandidateResult;
import com.sa.screening_service.dto.ScreeningResponse;
import com.sa.screening_service.dto.ShortlistResponse;
import com.sa.screening_service.entity.ScreeningJob;
import com.sa.screening_service.entity.ScreeningResult;
import com.sa.screening_service.entity.ScreeningStatus;
import com.sa.screening_service.repositoy.ScreeningJobRepository;
import com.sa.screening_service.repositoy.ScreeningResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ScreeningService {

    private final ScreeningJobRepository screeningJobRepository;
    private final ScreeningResultRepository screeningResultRepository;
    private final GeminiService geminiService;
    private final ScreeningResultMapper screeningResultMapper;

    public ScreeningService(
            ScreeningJobRepository screeningJobRepository,
            ScreeningResultRepository screeningResultRepository,
            GeminiService geminiService,
            ScreeningResultMapper screeningResultMapper) {

        this.screeningJobRepository = screeningJobRepository;
        this.screeningResultRepository = screeningResultRepository;
        this.geminiService = geminiService;
        this.screeningResultMapper = screeningResultMapper;
    }

    /*
     * FACADE PATTERN
     *
     * Controller does not coordinate the entire workflow.
     *
     * It delegates the screening operation to this service.
     */
    @Transactional
    public ScreeningJob analyze(
            String fileName,
            byte[] excelBytes,
            String jobDescription,
            String screeningCriteria) {

        validateInput(
                fileName,
                excelBytes,
                jobDescription,
                screeningCriteria
        );

        /*
         * Create history record immediately.
         *
         * This allows us to track the screening attempt
         * even if Gemini eventually fails.
         */
        ScreeningJob job = ScreeningJob.builder()
                .fileName(fileName)
                .jobDescription(jobDescription)
                .screeningCriteria(screeningCriteria)
                .status(ScreeningStatus.PROCESSING)
                .totalCandidates(0)
                .build();

        job = screeningJobRepository.save(job);

        try {

            /*
             * ORIGINAL EXCEL → GEMINI
             *
             * No local candidate extraction.
             */
            ScreeningResponse aiResponse =
                    geminiService.screenCandidates(
                            excelBytes,
                            fileName,
                            jobDescription,
                            screeningCriteria
                    );

            if (aiResponse == null ||
                    aiResponse.getCandidates() == null ||
                    aiResponse.getCandidates().isEmpty()) {

                throw new IllegalArgumentException(
                        "Gemini returned no candidates"
                );
            }

            List<CandidateResult> candidates =
                    new ArrayList<>(
                            aiResponse.getCandidates()
                    );

            /*
             * Defensive sorting.
             */
            candidates.sort(
                    Comparator.comparing(
                            CandidateResult::getScore,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    )
            );

            List<ScreeningResult> results =
                    new ArrayList<>();

            int rank = 1;

            for (CandidateResult candidate : candidates) {

                validateCandidate(candidate);

                ScreeningResult result =
                        screeningResultMapper.toEntity(
                                candidate,
                                job.getId(),
                                rank
                        );

                results.add(result);

                rank++;
            }

            /*
             * Persist all Gemini results.
             */
            screeningResultRepository.saveAll(results);

            /*
             * Update history.
             */
            job.setTotalCandidates(results.size());
            job.setStatus(ScreeningStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            return screeningJobRepository.save(job);

        } catch (Exception exception) {

            job.setStatus(ScreeningStatus.FAILED);

            screeningJobRepository.save(job);

            throw exception;
        }
    }

    /*
     * Returns the top X% of candidates.
     *
     * Example:
     *
     * 1,000 candidates
     * 10%
     * ↓
     * top 100
     */
    @Transactional(readOnly = true)
    public ShortlistResponse getShortlist(
            Long jobId,
            Integer percentage) {

        validatePercentage(percentage);

        ScreeningJob job =
                screeningJobRepository.findById(jobId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Screening job not found: "
                                                + jobId
                                )
                        );

        if (job.getStatus() != ScreeningStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Screening job is not completed yet"
            );
        }

        List<ScreeningResult> allResults =
                screeningResultRepository
                        .findByJobIdOrderByScoreDesc(jobId);

        int totalCandidates = allResults.size();

        int shortlistedCount =
                calculateShortlistedCount(
                        totalCandidates,
                        percentage
                );

        List<ScreeningResult> selected =
                allResults.subList(
                        0,
                        shortlistedCount
                );

        List<CandidateResult> candidates =
                selected.stream()
                        .map(this::toDto)
                        .toList();

        return new ShortlistResponse(
                jobId,
                percentage,
                totalCandidates,
                shortlistedCount,
                candidates
        );
    }

    private int calculateShortlistedCount(
            int totalCandidates,
            int percentage) {

        if (totalCandidates == 0) {
            return 0;
        }

        int count =
                (int) Math.ceil(
                        totalCandidates *
                                percentage /
                                100.0
                );

        return Math.max(count, 1);
    }

    private CandidateResult toDto(
            ScreeningResult result) {

        CandidateResult candidate =
                new CandidateResult();

        candidate.setName(result.getName());
        candidate.setEmail(result.getEmail());
        candidate.setScore(result.getScore());
        candidate.setRecommendation(
                result.getRecommendation()
        );
        candidate.setReason(result.getReason());
        candidate.setLinkedin(result.getLinkedin());
        candidate.setPortfolio(result.getPortfolio());
        candidate.setGithub(result.getGithub());
        candidate.setResume(result.getResume());
        candidate.setRank(result.getRank());

        return candidate;
    }

    private void validateInput(
            String fileName,
            byte[] excelBytes,
            String jobDescription,
            String screeningCriteria) {

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "File name is required"
            );
        }

        if (excelBytes == null ||
                excelBytes.length == 0) {

            throw new IllegalArgumentException(
                    "Excel file is required"
            );
        }

        if (jobDescription == null ||
                jobDescription.isBlank()) {

            throw new IllegalArgumentException(
                    "Job description is required"
            );
        }

        if (screeningCriteria == null ||
                screeningCriteria.isBlank()) {

            throw new IllegalArgumentException(
                    "Screening criteria is required"
            );
        }
    }

    private void validateCandidate(
            CandidateResult candidate) {

        if (candidate == null) {
            throw new IllegalArgumentException(
                    "Gemini returned a null candidate"
            );
        }

        if (candidate.getName() == null ||
                candidate.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Candidate name cannot be empty"
            );
        }

        if (candidate.getEmail() == null ||
                candidate.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Candidate email cannot be empty"
            );
        }

        if (candidate.getScore() == null) {

            throw new IllegalArgumentException(
                    "Candidate score cannot be null"
            );
        }

        if (candidate.getScore() < 0 ||
                candidate.getScore() > 100) {

            throw new IllegalArgumentException(
                    "Candidate score must be between 0 and 100"
            );
        }

        if (candidate.getRecommendation() == null ||
                candidate.getRecommendation().isBlank()) {

            throw new IllegalArgumentException(
                    "Candidate recommendation cannot be empty"
            );
        }

        if (candidate.getReason() == null ||
                candidate.getReason().isBlank()) {

            throw new IllegalArgumentException(
                    "Candidate reason cannot be empty"
            );
        }
    }

    private void validatePercentage(
            Integer percentage) {

        if (percentage == null ||
                percentage < 1 ||
                percentage > 100) {

            throw new IllegalArgumentException(
                    "Percentage must be between 1 and 100"
            );
        }
    }
}