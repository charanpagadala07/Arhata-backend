package com.sa.screening_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResult {

    private Integer sourceRow;

    private String name;

    private String email;

    private Integer score;

    private String recommendation;

    private String reason;

    private String linkedin;

    private String portfolio;

    private String github;

    private String resume;

    private Integer rank;
}