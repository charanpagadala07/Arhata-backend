package com.sa.screening_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "screening_jobs",
        indexes = {
                @Index(name = "idx_screening_job_status", columnList = "status"),
                @Index(name = "idx_screening_job_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "upload_id")
    private Long uploadId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(
            name = "job_description",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String jobDescription;


    @Column(
            name = "screening_criteria",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String screeningCriteria;

    @Column(name = "total_candidates")
    private Integer totalCandidates;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScreeningStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = ScreeningStatus.PROCESSING;
        }
    }
}