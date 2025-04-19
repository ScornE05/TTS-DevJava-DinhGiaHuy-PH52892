package com.example.staffmanagement.service;


import com.example.staffmanagement.DTO.ImportRecordStatus;
import com.example.staffmanagement.DTO.ImportResponseDTO;
import com.example.staffmanagement.entity.*;
import com.example.staffmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportExportService {

    private final StaffRepository staffRepository;
    private final ImportHistoryRepository importHistoryRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final DepartmentFacilityRepository departmentFacilityRepository;
    private final MajorFacilityRepository majorFacilityRepository;
    private final StaffMajorFacilityRepository staffMajorFacilityRepository;

    private final Pattern FPT_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@fpt\\.edu\\.vn$");
    private final Pattern FE_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@fe\\.edu\\.vn$");

    public byte[] downloadTemplate() throws IOException {
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

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }


    @Transactional
    public ImportResponseDTO importStaff(MultipartFile file) throws IOException {
        List<ImportRecordStatus> recordStatuses = new ArrayList<>();
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
                ImportRecordStatus recordStatus = new ImportRecordStatus();
                recordStatus.setRowNumber(i + 1);

                try {
                    String staffCode = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String accountFpt = getCellValueAsString(row.getCell(2));
                    String accountFe = getCellValueAsString(row.getCell(3));
                    String facilityCode = getCellValueAsString(row.getCell(4));
                    String departmentName = getCellValueAsString(row.getCell(5));
                    String majorName = getCellValueAsString(row.getCell(6));

                    recordStatus.setStaffCode(staffCode);
                    recordStatus.setName(name);
                    recordStatus.setAccountFpt(accountFpt);
                    recordStatus.setAccountFe(accountFe);

                    // Validate thông tin nhân viên
                    validateImportData(recordStatus);

                    // Lưu thông tin nhân viên
                    Staff staff = new Staff();
                    staff.setStaffCode(staffCode);
                    staff.setName(name);
                    staff.setAccountFpt(accountFpt);
                    staff.setAccountFe(accountFe);
                    staff.setStatus((byte) 1);

                    staff = staffRepository.save(staff);

                    // Import bộ môn chuyên ngành (nếu có)
                    if (!isNullOrEmpty(facilityCode) && !isNullOrEmpty(departmentName) && !isNullOrEmpty(majorName)) {
                        try {
                            importSpecialization(staff, facilityCode, departmentName, majorName);
                            importDetails.append("Row ").append(recordStatus.getRowNumber())
                                    .append(": Nhập thành công nhân viên và bộ môn chuyên ngành - ")
                                    .append(staffCode).append(" - ")
                                    .append(name).append(" - ")
                                    .append(facilityCode).append(" - ")
                                    .append(departmentName).append(" - ")
                                    .append(majorName).append("\n");
                        } catch (Exception e) {
                            importDetails.append("Row ").append(recordStatus.getRowNumber())
                                    .append(": Nhập thành công nhân viên nhưng lỗi khi nhập bộ môn chuyên ngành - ")
                                    .append(staffCode).append(" - ")
                                    .append(name).append(" - Lỗi: ")
                                    .append(e.getMessage()).append("\n");
                        }
                    } else {
                        importDetails.append("Row ").append(recordStatus.getRowNumber())
                                .append(": Nhập thành công nhân viên - ")
                                .append(staffCode).append(" - ")
                                .append(name).append("\n");
                    }

                    recordStatus.setSuccess(true);
                    successfulRecords++;

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

            // Lưu lịch sử import
            ImportHistory importHistory = new ImportHistory();
            importHistory.setFileName(file.getOriginalFilename());
            importHistory.setImportDate(LocalDateTime.now());
            importHistory.setTotalRecords(totalRecords);
            importHistory.setSuccessfulRecords(successfulRecords);
            importHistory.setFailedRecords(failedRecords);
            importHistory.setImportDetails(importDetails.toString());
            importHistory = importHistoryRepository.save(importHistory);

            // Tạo response
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

    private void importSpecialization(Staff staff, String facilityCode, String departmentName, String majorName) {
        // Tìm cơ sở theo code
        Facility facility = facilityRepository.findByCode(facilityCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cơ sở với mã: " + facilityCode));

        // Tìm bộ môn theo tên và facility
        List<Department> departments = departmentRepository.findAllByFacilityId(facility.getId())
                .stream()
                .filter(d -> d.getName().equalsIgnoreCase(departmentName))
                .collect(Collectors.toList());

        if (departments.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bộ môn '" + departmentName + "' trong cơ sở " + facilityCode);
        }

        Department department = departments.get(0);

        // Tìm chuyên ngành theo tên, bộ môn và facility
        List<Major> majors = majorRepository.findAllByDepartmentIdAndFacilityId(department.getId(), facility.getId())
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase(majorName))
                .collect(Collectors.toList());

        if (majors.isEmpty()) {
            throw new RuntimeException("Không tìm thấy chuyên ngành '" + majorName + "' trong bộ môn '" + departmentName + "' và cơ sở " + facilityCode);
        }

        Major major = majors.get(0);

        // Kiểm tra xem nhân viên đã có chuyên ngành trong cơ sở này chưa
        List<StaffMajorFacility> existingSpecializations = staffMajorFacilityRepository
                .findByStaffIdAndFacilityId(staff.getId(), facility.getId());

        if (!existingSpecializations.isEmpty()) {
            throw new RuntimeException("Nhân viên đã có chuyên ngành trong cơ sở " + facility.getName());
        }

        // Tìm hoặc tạo DepartmentFacility
        DepartmentFacility departmentFacility = getDepartmentFacility(department, facility);

        // Tìm hoặc tạo MajorFacility
        MajorFacility majorFacility = getMajorFacility(major, departmentFacility);

        // Tạo StaffMajorFacility
        StaffMajorFacility staffMajorFacility = new StaffMajorFacility();
        staffMajorFacility.setStaff(staff);
        staffMajorFacility.setMajorFacility(majorFacility);
        staffMajorFacility.setStatus((byte) 1);

        staffMajorFacilityRepository.save(staffMajorFacility);
    }

    private DepartmentFacility getDepartmentFacility(Department department, Facility facility) {
        Optional<DepartmentFacility> optionalDeptFacility = departmentFacilityRepository
                .findByDepartmentAndFacility(department, facility);

        if (optionalDeptFacility.isPresent()) {
            return optionalDeptFacility.get();
        } else {
            DepartmentFacility departmentFacility = new DepartmentFacility();
            departmentFacility.setDepartment(department);
            departmentFacility.setFacility(facility);
            departmentFacility.setStatus((byte) 1);
            return departmentFacilityRepository.save(departmentFacility);
        }
    }

    private MajorFacility getMajorFacility(Major major, DepartmentFacility departmentFacility) {
        Optional<MajorFacility> optionalMajorFacility = majorFacilityRepository
                .findByMajorAndDepartmentFacility(major, departmentFacility);

        if (optionalMajorFacility.isPresent()) {
            return optionalMajorFacility.get();
        } else {
            MajorFacility majorFacility = new MajorFacility();
            majorFacility.setMajor(major);
            majorFacility.setDepartmentFacility(departmentFacility);
            majorFacility.setStatus((byte) 1);
            return majorFacilityRepository.save(majorFacility);
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

        ImportResponseDTO dto = convertToDTO(importHistory);

        // Phân tích ImportDetails để tạo danh sách recordStatuses
        List<ImportRecordStatus> recordStatuses = new ArrayList<>();
        if (importHistory.getImportDetails() != null) {
            String[] lines = importHistory.getImportDetails().split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                ImportRecordStatus status = new ImportRecordStatus();

                // Lấy số dòng
                int rowStartIndex = line.indexOf("Row ") + 4;
                int rowEndIndex = line.indexOf(":", rowStartIndex);
                if (rowStartIndex >= 4 && rowEndIndex > rowStartIndex) {
                    try {
                        status.setRowNumber(Integer.parseInt(line.substring(rowStartIndex, rowEndIndex).trim()));
                    } catch (NumberFormatException e) {
                        status.setRowNumber(0);
                    }
                }

                // Xác định thành công hay thất bại
                boolean isSuccess = line.contains("Nhập thành công");
                status.setSuccess(isSuccess);

                // Lấy thông tin lỗi nếu có
                if (!isSuccess) {
                    int errorIndex = line.indexOf("Lỗi - ");
                    if (errorIndex >= 0) {
                        status.setErrorMessage(line.substring(errorIndex + 6).trim());
                    } else {
                        status.setErrorMessage("Lỗi không xác định");
                    }
                }

                // Lấy thông tin nhân viên
                if (isSuccess) {
                    int infoStartIndex = line.indexOf("Nhập thành công") + "Nhập thành công".length();
                    String infoText = line.substring(infoStartIndex).trim();

                    String[] parts = infoText.split(" - ");
                    if (parts.length >= 2) {
                        // Phần đầu tiên có thể là "nhân viên" hoặc "nhân viên và bộ môn chuyên ngành"
                        // Phần thứ 2 là mã nhân viên
                        status.setStaffCode(parts[1]);
                        if (parts.length >= 3) {
                            status.setName(parts[2]);
                        }
                    }
                }

                recordStatuses.add(status);
            }
        }

        dto.setRecordStatuses(recordStatuses);
        return dto;
    }

    private ImportResponseDTO convertToDTO(ImportHistory importHistory) {
        ImportResponseDTO dto = new ImportResponseDTO();
        dto.setId(importHistory.getId());
        dto.setFileName(importHistory.getFileName());
        dto.setImportDate(importHistory.getImportDate());
        dto.setTotalRecords(importHistory.getTotalRecords());
        dto.setSuccessfulRecords(importHistory.getSuccessfulRecords());
        dto.setFailedRecords(importHistory.getFailedRecords());

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

    private void validateImportData(ImportRecordStatus record) {
        // Kiểm tra trường trống
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

        // Kiểm tra độ dài
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

        // Kiểm tra định dạng email
        if (!FPT_EMAIL_PATTERN.matcher(record.getAccountFpt()).matches()) {
            throw new RuntimeException("Email FPT phải có định dạng xxx@fpt.edu.vn");
        }

        if (!FE_EMAIL_PATTERN.matcher(record.getAccountFe()).matches()) {
            throw new RuntimeException("Email FE phải có định dạng xxx@fe.edu.vn");
        }

        // Kiểm tra email chứa mã nhân viên
        String staffCodeLower = record.getStaffCode().toLowerCase();
        if (!record.getAccountFpt().toLowerCase().contains(staffCodeLower)) {
            throw new RuntimeException("Email FPT phải chứa mã nhân viên");
        }

        if (!record.getAccountFe().toLowerCase().contains(staffCodeLower)) {
            throw new RuntimeException("Email FE phải chứa mã nhân viên");
        }

        // Kiểm tra trùng lặp
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
    public byte[] exportStaffData() throws IOException {
        List<Staff> staffList = staffRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách nhân viên");

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] columns = {"Mã nhân viên", "Họ và tên", "Email FPT", "Email FE", "Trạng thái"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Tạo các dòng dữ liệu
            int rowNum = 1;
            for (Staff staff : staffList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(staff.getStaffCode());
                row.createCell(1).setCellValue(staff.getName());
                row.createCell(2).setCellValue(staff.getAccountFpt());
                row.createCell(3).setCellValue(staff.getAccountFe());
                row.createCell(4).setCellValue(staff.getStatus() == 1 ? "Đang hoạt động" : "Ngừng hoạt động");
            }

            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Chuyển workbook thành mảng byte
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}