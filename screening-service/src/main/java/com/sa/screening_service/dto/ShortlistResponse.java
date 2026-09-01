package com.sa.screening_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortlistResponse {

    private Long jobId;

    private Integer percentage;

    private Integer totalCandidates;

    private Integer shortlistedCandidates;

    private List<CandidateResult> candidates;
}