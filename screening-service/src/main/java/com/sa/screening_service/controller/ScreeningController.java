package com.sa.screening_service.controller;

import com.sa.screening_service.dto.ShortlistResponse;
import com.sa.screening_service.entity.ScreeningJob;
import com.sa.screening_service.service.ScreeningService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/screening")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok(
                "Screening Service is accessible. JWT authentication successful."
        );
    }


    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ScreeningJob> analyze(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("jobDescription")
            String jobDescription,

            @RequestParam("screeningCriteria")
            String screeningCriteria) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel file is required"
            );
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Upload an .xlsx Excel workbook");
        }

        ScreeningJob result =
                screeningService.analyze(
                        fileName,
                        file.getBytes(),
                        jobDescription,
                        screeningCriteria
                );
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{jobId}/shortlist")
    public ResponseEntity<ShortlistResponse> getShortlist(

            @PathVariable Long jobId,

            @RequestParam Integer percentage) {

        ShortlistResponse response =
                screeningService.getShortlist(
                        jobId,
                        percentage
                );

        return ResponseEntity.ok(response);
    }
}