package com.example.staffmanagement.controller.web;

import com.example.staffmanagement.DTO.ImportResponseDTO;
import com.example.staffmanagement.service.ImportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/import-export")
@RequiredArgsConstructor
public class ImportExportWebController {

    private final ImportExportService importExportService;

    // Trang import/export
    @GetMapping
    public String importExportPage() {
        return "import/index";
    }

    // Tải template
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(HttpServletResponse response) throws IOException {
        byte[] templateBytes = importExportService.downloadTemplate(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "staff_import_template.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(templateBytes);
    }

    // Form import nhân viên
    @GetMapping("/import")
    public String importForm() {
        return "import/import-form";
    }

    // Xử lý import nhân viên
    @PostMapping("/import")
    public String importStaff(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            ImportResponseDTO result = importExportService.importStaff(file);
            redirectAttributes.addFlashAttribute("importResult", result);
            redirectAttributes.addFlashAttribute("success",
                    "Import thành công " + result.getSuccessfulRecords() + "/" + result.getTotalRecords() + " bản ghi");
            return "redirect:/import-export/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi import: " + e.getMessage());
            return "redirect:/import-export/import";
        }
    }

    // Danh sách lịch sử import
    @GetMapping("/history")
    public String importHistory(Model model) {
        List<ImportResponseDTO> historyList = importExportService.getAllImportHistory();
        model.addAttribute("historyList", historyList);
        return "import/history";
    }

    // Chi tiết lịch sử import
    @GetMapping("/history/{id}")
    public String importHistoryDetail(@PathVariable UUID id, Model model) {
        ImportResponseDTO historyDetail = importExportService.getImportHistoryById(id);
        model.addAttribute("history", historyDetail);
        return "import/detail";
    }
}