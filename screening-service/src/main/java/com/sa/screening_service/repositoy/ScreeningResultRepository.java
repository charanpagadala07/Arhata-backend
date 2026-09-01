package com.sa.screening_service.repositoy;

import com.sa.screening_service.entity.ScreeningResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningResultRepository
        extends JpaRepository<ScreeningResult, Long> {

    List<ScreeningResult> findByJobIdOrderByScoreDesc(
            Long jobId
    );

    List<ScreeningResult> findByJobIdOrderByRankAsc(
            Long jobId
    );

    long countByJobId(Long jobId);
}