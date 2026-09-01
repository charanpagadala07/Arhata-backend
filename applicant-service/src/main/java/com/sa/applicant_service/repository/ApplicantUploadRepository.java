package com.sa.applicant_service.repository;

import com.sa.applicant_service.entity.ApplicantUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantUploadRepository
        extends JpaRepository<ApplicantUpload, Long> {

    List<ApplicantUpload> findByUserId(Long userId);
}