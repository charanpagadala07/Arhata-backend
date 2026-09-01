package com.sa.applicant_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadResponse {

    private Long uploadId;
    private String fileName;
    private String status;
}