package com.example.staffmanagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponseDTO {

    private UUID id;
    private String fileName;
    private LocalDateTime importDate;
    private Integer totalRecords;
    private Integer successfulRecords;
    private Integer failedRecords;
    private List<ImportRecordStatus> recordStatuses;

}