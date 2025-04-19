package com.example.staffmanagement.service;


import com.example.staffmanagement.DTO.ImportResponseDTO;
import com.example.staffmanagement.DTO.StaffDTO;
import com.example.staffmanagement.entity.ImportHistory;
import com.example.staffmanagement.entity.Staff;
import com.example.staffmanagement.repository.ImportHistoryRepository;
import com.example.staffmanagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportExportService {

    private final StaffRepository staffRepository;
    private final ImportHistoryRepository importHistoryRepository;

    private final Pattern FPT_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@fpt\\.edu\\.vn$");
    private final Pattern FE_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@fe\\.edu\\.vn$");

    public byte[] downloadTemplate(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Staff Import Template");

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] columns = {"Mã nhân viên", "Họ và tên", "Email FPT", "Email FE", "Cơ sở", "Bộ môn", "Chuyên ngành"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("NV001");
            exampleRow.createCell(1).setCellValue("Nguyen Van A");
            exampleRow.createCell(2).setCellValue("anv@fpt.edu.vn");
            exampleRow.createCell(3).setCellValue("anv@fe.edu.vn");
            exampleRow.createCell(4).setCellValue("HN");
            exampleRow.createCell(5).setCellValue("Department One");
            exampleRow.createCell(6).setCellValue("Major One");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Transactional
    public ImportResponseDTO importStaff(MultipartFile file) throws IOException {
        List<ImportResponseDTO.ImportRecordStatus> recordStatuses = new ArrayList<>();
        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;
        StringBuilder importDetails = new StringBuilder();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalRecords++;
                ImportResponseDTO.ImportRecordStatus recordStatus = new ImportResponseDTO.ImportRecordStatus();
                recordStatus.setRowNumber(i + 1);

                try {
                    String staffCode = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String accountFpt = getCellValueAsString(row.getCell(2));
                    String accountFe = getCellValueAsString(row.getCell(3));

                    recordStatus.setStaffCode(staffCode);
                    recordStatus.setName(name);
                    recordStatus.setAccountFpt(accountFpt);
                    recordStatus.setAccountFe(accountFe);
                    validateImportData(recordStatus);
                    Staff staff = new Staff();
                    staff.setStaffCode(staffCode);
                    staff.setName(name);
                    staff.setAccountFpt(accountFpt);
                    staff.setAccountFe(accountFe);
                    staff.setStatus((byte) 1);

                    staffRepository.save(staff);
                    recordStatus.setSuccess(true);
                    successfulRecords++;
                    importDetails.append("Row ").append(recordStatus.getRowNumber())
                            .append(": Nhập thành công - ")
                            .append(staffCode).append(" - ")
                            .append(name).append("\n");

                } catch (Exception e) {
                    // Mark as failed
                    recordStatus.setSuccess(false);
                    recordStatus.setErrorMessage(e.getMessage());
                    failedRecords++;
                    importDetails.append("Row ").append(recordStatus.getRowNumber())
                            .append(": Lỗi - ")
                            .append(e.getMessage()).append("\n");
                }

                recordStatuses.add(recordStatus);
            }
            ImportHistory importHistory = new ImportHistory();
            importHistory.setFileName(file.getOriginalFilename());
            importHistory.setImportDate(LocalDateTime.now());
            importHistory.setTotalRecords(totalRecords);
            importHistory.setSuccessfulRecords(successfulRecords);
            importHistory.setFailedRecords(failedRecords);
            importHistory.setImportDetails(importDetails.toString());
            importHistory = importHistoryRepository.save(importHistory);
            ImportResponseDTO response = new ImportResponseDTO();
            response.setId(importHistory.getId());
            response.setFileName(importHistory.getFileName());
            response.setImportDate(importHistory.getImportDate());
            response.setTotalRecords(importHistory.getTotalRecords());
            response.setSuccessfulRecords(importHistory.getSuccessfulRecords());
            response.setFailedRecords(importHistory.getFailedRecords());
            response.setRecordStatuses(recordStatuses);

            return response;
        }
    }

    public List<ImportResponseDTO> getAllImportHistory() {
        return importHistoryRepository.findAllOrderByImportDateDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ImportResponseDTO getImportHistoryById(UUID id) {
        ImportHistory importHistory = importHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử import với ID: " + id));

        return convertToDTO(importHistory);
    }

    private ImportResponseDTO convertToDTO(ImportHistory importHistory) {
        ImportResponseDTO dto = new ImportResponseDTO();
        dto.setId(importHistory.getId());
        dto.setFileName(importHistory.getFileName());
        dto.setImportDate(importHistory.getImportDate());
        dto.setTotalRecords(importHistory.getTotalRecords());
        dto.setSuccessfulRecords(importHistory.getSuccessfulRecords());
        dto.setFailedRecords(importHistory.getFailedRecords());
        List<ImportResponseDTO.ImportRecordStatus> recordStatuses = new ArrayList<>();
        dto.setRecordStatuses(recordStatuses);

        return dto;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private void validateImportData(ImportResponseDTO.ImportRecordStatus record) {
        // Check empty fields
        if (isNullOrEmpty(record.getStaffCode())) {
            throw new RuntimeException("Mã nhân viên không được để trống");
        }

        if (isNullOrEmpty(record.getName())) {
            throw new RuntimeException("Tên nhân viên không được để trống");
        }

        if (isNullOrEmpty(record.getAccountFpt())) {
            throw new RuntimeException("Email FPT không được để trống");
        }

        if (isNullOrEmpty(record.getAccountFe())) {
            throw new RuntimeException("Email FE không được để trống");
        }

        if (record.getStaffCode().length() > 15) {
            throw new RuntimeException("Mã nhân viên không được vượt quá 15 ký tự");
        }

        if (record.getName().length() > 100) {
            throw new RuntimeException("Tên nhân viên không được vượt quá 100 ký tự");
        }

        if (record.getAccountFpt().length() > 100) {
            throw new RuntimeException("Email FPT không được vượt quá 100 ký tự");
        }

        if (record.getAccountFe().length() > 100) {
            throw new RuntimeException("Email FE không được vượt quá 100 ký tự");
        }

        if (!FPT_EMAIL_PATTERN.matcher(record.getAccountFpt()).matches()) {
            throw new RuntimeException("Email FPT phải có định dạng xxx@fpt.edu.vn");
        }

        if (!FE_EMAIL_PATTERN.matcher(record.getAccountFe()).matches()) {
            throw new RuntimeException("Email FE phải có định dạng xxx@fe.edu.vn");
        }

        String staffCodeLower = record.getStaffCode().toLowerCase();
        if (!record.getAccountFpt().toLowerCase().contains(staffCodeLower) ||
                !record.getAccountFe().toLowerCase().contains(staffCodeLower)) {
            throw new RuntimeException("Email FPT và FE phải chứa mã nhân viên");
        }

        if (staffRepository.existsByStaffCode(record.getStaffCode())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại: " + record.getStaffCode());
        }

        if (staffRepository.existsByAccountFpt(record.getAccountFpt())) {
            throw new RuntimeException("Email FPT đã tồn tại: " + record.getAccountFpt());
        }

        if (staffRepository.existsByAccountFe(record.getAccountFe())) {
            throw new RuntimeException("Email FE đã tồn tại: " + record.getAccountFe());
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}