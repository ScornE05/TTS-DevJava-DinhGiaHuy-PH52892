package com.example.staffmanagement.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {

    private UUID id;

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 15, message = "Mã nhân viên không được vượt quá 15 ký tự")
    private String staffCode;

    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(max = 100, message = "Tên nhân viên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Email FPT không được để trống")
    @Size(max = 100, message = "Email FPT không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@fpt\\.edu\\.vn$",
            message = "Email FPT phải có định dạng xxx@fpt.edu.vn và không được chứa khoảng trắng hoặc ký tự tiếng Việt")
    private String accountFpt;

    @NotBlank(message = "Email FE không được để trống")
    @Size(max = 100, message = "Email FE không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@fe\\.edu\\.vn$",
            message = "Email FE phải có định dạng xxx@fe.edu.vn và không được chứa khoảng trắng hoặc ký tự tiếng Việt")
    private String accountFe;

    private Byte status;
}