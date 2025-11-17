package com.ai.rpa_review.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Iterator;


@Service
public class AIService {

    private final VertexAiGeminiChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIService( VertexAiGeminiChatModel vertexAiGeminiChatModel){
        this.chatModel = vertexAiGeminiChatModel;
    }
//    public String validateRpaFile(String fileName, String fileContent) {
//
//        String toolType = fileName.endsWith(".bp") ? "BluePrism" : "UiPath";
//
//        String promptText = """
//            You are an expert RPA code reviewer specializing in %s development.
//            The following is the workflow file content.
//
//            Task:
//            - Analyze its design and logic.
//            - Identify compliance or design issues.
//            - Provide best practice recommendations.
//            - Output ONLY valid JSON with this schema:
//              {
//                "tool": "<UiPath or Blue Prism>",
//                "compliance_score": <0-100>,
//                "issues": ["issue1", "issue2", ...],
//                "recommendations": ["rec1", "rec2", ...]
//              }
//
//            Workflow content:
//            %s
//            """.formatted(toolType, fileContent);
//
//        Prompt prompt = new Prompt(promptText);
//        ChatResponse response = chatModel.call(prompt);
//
//        System.out.println(response);
//
//        return "";
//    }

    public String validateRpaFile(String fileName, String fileContent) {
        try {
            // Detect tool and call Gemini
            Prompt prompt = getPrompt(fileName, fileContent);
            ChatResponse response = chatModel.call(prompt);

            String textContent = response.getResults().get(0).getOutput().getText();

            String cleanedJson = textContent
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            JsonNode jsonNode = objectMapper.readTree(cleanedJson);

            // Format as JSON
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

            // Export to Excel (auto-save file)
            String excelResult = exportToExcel(prettyJson, "RPA_Report.xlsx");

            return """
            {
              "status": "success",
              "excel_status": "%s",
              "gemini_response": %s
            }
            """.formatted(excelResult, prettyJson);

        } catch (Exception e) {
            return """
            {
              "error": "Failed to parse Gemini response",
              "details": "%s"
            }
            """.formatted(e.getMessage());
        }
    }

    private static Prompt getPrompt(String fileName, String fileContent) {
        String toolType;
        if (fileName.endsWith(".bpprocess") || fileName.endsWith(".bpobject") || fileName.endsWith(".bp") || fileName.endsWith(".bprelease")) {
            toolType = "Blue Prism";
        } else {
            toolType = "UiPath";
        }

        // Build the AI prompt
        String promptText = """
            You are an expert RPA code reviewer specializing in %s development.
            Analyze the following workflow file.

            Respond ONLY with valid JSON in this exact schema:
            {
              "tool": "<UiPath or Blue Prism>",
              "compliance_score": <0-100>,
              "issues": ["issue1", "issue2", ...],
              "recommendations": ["rec1", "rec2", ...]
            }

            Workflow content:
            %s
            """.formatted(toolType, fileContent);

        // Call Gemini model
        Prompt prompt = new Prompt(promptText);
        System.out.println();
        return prompt;
    }

    public String exportToExcel(String jsonResponse, String outputFileName) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            String tool = jsonNode.path("tool").asText();
            int complianceScore = jsonNode.path("compliance_score").asInt();

            // Get issues and recommendations as arrays
            JsonNode issuesNode = jsonNode.path("issues");
            JsonNode recNode = jsonNode.path("recommendations");

            int issueCount = issuesNode.isArray() ? issuesNode.size() : 0;
            int recCount = recNode.isArray() ? recNode.size() : 0;
            int totalRows = Math.max(issueCount, recCount);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Gemini Results");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tool");
            header.createCell(1).setCellValue("Compliance Score");
            header.createCell(2).setCellValue("Issues");
            header.createCell(3).setCellValue("Recommendations");

            // Create a style for vertical centering and wrap
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // Populate rows
            for (int i = 0; i < totalRows; i++) {
                Row row = sheet.createRow(i + 1);

                // Tool and compliance_score should appear only on first row
                if (i == 0) {
                    row.createCell(0).setCellValue(tool);
                    row.createCell(1).setCellValue(complianceScore);
                }

                // Issues column
                if (i < issueCount) {
                    Cell issueCell = row.createCell(2);
                    issueCell.setCellValue(issuesNode.get(i).asText());
                    issueCell.setCellStyle(wrapStyle);
                }

                // Recommendations column
                if (i < recCount) {
                    Cell recCell = row.createCell(3);
                    recCell.setCellValue(recNode.get(i).asText());
                    recCell.setCellStyle(wrapStyle);
                }
            }

            // Merge cells for Tool and Compliance Score vertically
            if (totalRows > 1) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, totalRows, 0, 0)); // Merge Tool
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, totalRows, 1, 1)); // Merge Compliance Score
            }

            // Center align vertically for merged cells
            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Row firstDataRow = sheet.getRow(1);
            if (firstDataRow != null) {
                if (firstDataRow.getCell(0) != null)
                    firstDataRow.getCell(0).setCellStyle(centerStyle);
                if (firstDataRow.getCell(1) != null)
                    firstDataRow.getCell(1).setCellStyle(centerStyle);
            }

            // Auto-size columns for neat layout
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save Excel file
            String path = Paths.get(System.getProperty("user.dir"), outputFileName).toString();
            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }
            workbook.close();

            return "Excel file saved successfully: " + path;

        } catch (Exception e) {
            return "Failed to export Excel: " + e.getMessage();
        }
    }
}
