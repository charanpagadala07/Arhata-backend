package com.sa.screening_service.repositoy;

import com.sa.screening_service.entity.ScreeningJob;
import com.sa.screening_service.entity.ScreeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningJobRepository
        extends JpaRepository<ScreeningJob, Long> {

    List<ScreeningJob> findByStatusOrderByCreatedAtDesc(
            ScreeningStatus status
    );

    List<ScreeningJob> findAllByOrderByCreatedAtDesc();
}