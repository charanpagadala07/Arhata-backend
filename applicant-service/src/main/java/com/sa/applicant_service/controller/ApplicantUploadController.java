package com.sa.applicant_service.controller;

import com.sa.applicant_service.dto.UploadResponse;
import com.sa.applicant_service.service.ApplicantUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applicants")
public class ApplicantUploadController {

    private final ApplicantUploadService uploadService;

    public ApplicantUploadController(
            ApplicantUploadService uploadService) {

        this.uploadService = uploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        UploadResponse response =
                uploadService.uploadExcel(file, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}