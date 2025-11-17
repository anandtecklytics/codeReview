package com.ai.rpa_review.controller;

import com.ai.rpa_review.service.AIService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }


    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> validateFile(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1 — check if file exists
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"No file uploaded. Please select a file to validate.\"}");
            }

            // Step 2 — check extension
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (!(extension.equals("bpprocess") || extension.equals("bpobject") || extension.equals("xml") || extension.equals("bprelease"))) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Invalid file type. Please upload a .bpprocess, .bpobject, or .xml file.\"}");
            }

            // Step 3 — read content
            String fileContent = new String(file.getBytes(), StandardCharsets.UTF_8);

            // Step 4 — pass to AI service
            String aiResult = aiService.validateRpaFile(fileName, fileContent);

            // Step 5 — return JSON response
            return ResponseEntity.ok(aiResult);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error reading file: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }


}
