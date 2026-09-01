package com.sa.applicant_service.service;

import com.sa.applicant_service.dto.UploadResponse;
import com.sa.applicant_service.entity.ApplicantUpload;
import com.sa.applicant_service.repository.ApplicantUploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class ApplicantUploadService {

    private final ApplicantUploadRepository uploadRepository;

    private static final String UPLOAD_DIRECTORY = "uploads";

    public ApplicantUploadService(
            ApplicantUploadRepository uploadRepository) {

        this.uploadRepository = uploadRepository;
    }

    public UploadResponse uploadExcel(
            MultipartFile file,
            Long userId) {

        validateFile(file);

        try {

            Path uploadDirectory =
                    Paths.get(UPLOAD_DIRECTORY);

            Files.createDirectories(uploadDirectory);

            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + file.getOriginalFilename();

            Path filePath =
                    uploadDirectory.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            ApplicantUpload upload =
                    new ApplicantUpload();

            upload.setUserId(userId);
            upload.setFileName(file.getOriginalFilename());
            upload.setFilePath(filePath.toString());
            upload.setStatus("UPLOADED");

            ApplicantUpload saved =
                    uploadRepository.save(upload);

            return new UploadResponse(
                    saved.getId(),
                    saved.getFileName(),
                    saved.getStatus()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to store uploaded file",
                    e
            );
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel file is required"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !(fileName.toLowerCase().endsWith(".xlsx")
                        || fileName.toLowerCase().endsWith(".xls"))) {

            throw new IllegalArgumentException(
                    "Only Excel files are allowed"
            );
        }
    }
}