package com.sa.screening_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "screening_results",
        indexes = {
                @Index(
                        name = "idx_result_job_id",
                        columnList = "job_id"
                ),
                @Index(
                        name = "idx_result_job_score",
                        columnList = "job_id, score"
                ),
                @Index(
                        name = "idx_result_email",
                        columnList = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "candidate_rank", nullable = false)
    private Integer rank;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "recommendation", nullable = false, length = 30)
    private String recommendation;

    @Column(
            name = "reason",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String reason;

    @Column(name = "linkedin", length = 500)
    private String linkedin;

    @Column(name = "portfolio", length = 500)
    private String portfolio;

    @Column(name = "github", length = 500)
    private String github;

    @Column(name = "resume", length = 500)
    private String resume;
}