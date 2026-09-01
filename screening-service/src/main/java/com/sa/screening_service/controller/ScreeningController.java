package com.sa.screening_service.controller;

import com.sa.screening_service.dto.ShortlistResponse;
import com.sa.screening_service.entity.ScreeningJob;
import com.sa.screening_service.service.ScreeningService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            String screeningCriteria) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel file is required"
            );
        }

        try {

            ScreeningJob result =
                    screeningService.analyze(
                            file.getOriginalFilename(),
                            file.getBytes(),
                            jobDescription,
                            screeningCriteria
                    );

            return ResponseEntity.ok(result);

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Screening failed: "
                            + exception.getMessage(),
                    exception
            );
        }
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