package com.example.staffmanagement.controller;

import com.example.staffmanagement.DTO.ImportResponseDTO;
import com.example.staffmanagement.service.ImportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import-export")
@RequiredArgsConstructor
public class ImportExportController {

    private final ImportExportService importExportService;

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] templateBytes = importExportService.downloadTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "staff_import_template.xlsx");

        return new ResponseEntity<>(templateBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/staff")
    public ResponseEntity<ImportResponseDTO> importStaff(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(importExportService.importStaff(file));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportResponseDTO>> getImportHistory() {
        return ResponseEntity.ok(importExportService.getAllImportHistory());
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<ImportResponseDTO> getImportHistoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(importExportService.getImportHistoryById(id));
    }
}
