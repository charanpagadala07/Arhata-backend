package com.sa.screening_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelServiceTest {

    @Test
    void readsOneThousandCandidatesInBatches() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Applicants");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Email");

            for (int index = 0; index < 1_000; index++) {
                var row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue("Candidate " + (index + 1));
                row.createCell(1).setCellValue("candidate" + (index + 1) + "@example.com");
            }

            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> batches = new ExcelService(objectMapper)
                .readCandidateBatches(workbookBytes, 25);

        assertEquals(40, batches.size());
        JsonNode firstCandidate = objectMapper.readTree(batches.get(0)).get(0);
        JsonNode lastCandidate = objectMapper.readTree(batches.get(39)).get(24);
        assertEquals("2", firstCandidate.get("_sourceRow").asText());
        assertEquals("Candidate 1", firstCandidate.get("Name").asText());
        assertEquals("1001", lastCandidate.get("_sourceRow").asText());
        assertEquals("Candidate 1000", lastCandidate.get("Name").asText());
    }
}
