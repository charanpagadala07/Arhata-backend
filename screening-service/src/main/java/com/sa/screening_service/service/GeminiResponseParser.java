package com.sa.screening_service.service;

import com.sa.screening_service.dto.ScreeningResponse;

public interface GeminiResponseParser {

    ScreeningResponse parse(String rawResponse);
}