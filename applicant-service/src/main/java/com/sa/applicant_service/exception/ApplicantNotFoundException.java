package com.sa.applicant_service.exception;

public class ApplicantNotFoundException extends RuntimeException {

    public ApplicantNotFoundException(String message) {
        super(message);
    }
}