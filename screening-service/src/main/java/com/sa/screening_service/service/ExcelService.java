package com.sa.screening_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    private final ObjectMapper objectMapper;

    public ExcelService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> readCandidateBatches(byte[] excelBytes, int batchSize) {
        if (excelBytes == null || excelBytes.length == 0) {
            throw new IllegalArgumentException("Excel file is empty");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size must be positive");
        }

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Excel workbook has no worksheets");
            }

            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter();
            Row headerRow = findHeaderRow(sheet, formatter, evaluator);
            if (headerRow == null) {
                throw new IllegalArgumentException("The first worksheet does not contain a header row");
            }

            List<String> headers = readHeaders(headerRow, formatter, evaluator);
            List<Map<String, String>> rows = new ArrayList<>();
            List<String> batches = new ArrayList<>();
            int lastColumn = headerRow.getLastCellNum();

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(row, lastColumn, formatter, evaluator)) {
                    continue;
                }

                Map<String, String> candidate = new LinkedHashMap<>();
                candidate.put("_sourceRow", Integer.toString(rowIndex + 1));
                for (int column = 0; column < headers.size(); column++) {
                    Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String value = cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
                    candidate.put(headers.get(column), value);
                }
                rows.add(candidate);

                if (rows.size() == batchSize) {
                    batches.add(toJson(rows));
                    rows = new ArrayList<>(batchSize);
                }
            }

            if (!rows.isEmpty()) {
                batches.add(toJson(rows));
            }
            if (batches.isEmpty()) {
                throw new IllegalArgumentException("The first worksheet does not contain any candidate rows");
            }
            return batches;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read the uploaded Excel workbook: " + exception.getMessage(), exception);
        }
    }

    private Row findHeaderRow(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (Row row : sheet) {
            if (!isBlank(row, Math.max(0, row.getLastCellNum()), formatter, evaluator)) {
                return row;
            }
        }
        return null;
    }

    private List<String> readHeaders(Row headerRow, DataFormatter formatter, FormulaEvaluator evaluator) {
        short lastCell = headerRow.getLastCellNum();
        List<String> headers = new ArrayList<>(Math.max(0, lastCell));
        Map<String, Integer> used = new LinkedHashMap<>();

        for (int column = 0; column < lastCell; column++) {
            Cell cell = headerRow.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String header = cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
            if (header.isBlank()) {
                header = "Column " + (column + 1);
            }

            int occurrence = used.merge(header, 1, Integer::sum);
            headers.add(occurrence == 1 ? header : header + " (" + occurrence + ")");
        }
        return headers;
    }

    private boolean isBlank(Row row, int lastColumn, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (int column = 0; column < lastColumn; column++) {
            Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !formatter.formatCellValue(cell, evaluator).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String toJson(List<Map<String, String>> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to prepare candidate rows for screening", exception);
        }
    }
}
