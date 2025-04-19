package com.example.staffmanagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRecordStatus {
    private Integer rowNumber;
    private String staffCode;
    private String name;
    private String accountFpt;
    private String accountFe;
    private Boolean success;
    private String errorMessage;
}
