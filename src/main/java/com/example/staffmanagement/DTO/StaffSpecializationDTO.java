package com.example.staffmanagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffSpecializationDTO {

    private UUID id;

    @NotNull(message = "ID nhân viên không được để trống")
    private UUID staffId;

    @NotNull(message = "ID cơ sở không được để trống")
    private UUID facilityId;

    @NotNull(message = "ID bộ môn không được để trống")
    private UUID departmentId;

    @NotNull(message = "ID chuyên ngành không được để trống")
    private UUID majorId;

    // Thông tin bổ sung cho hiển thị
    private String staffName;
    private String facilityName;
    private String departmentName;
    private String majorName;
}