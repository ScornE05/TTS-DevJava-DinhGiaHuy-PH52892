package com.example.staffmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate;

    @Column(name = "total_records", nullable = false)
    private Integer totalRecords;

    @Column(name = "successful_records", nullable = false)
    private Integer successfulRecords;

    @Column(name = "failed_records", nullable = false)
    private Integer failedRecords;

    @Column(name = "import_details", columnDefinition = "TEXT")
    private String importDetails;

    @PrePersist
    protected void onCreate() {
        if (importDate == null) {
            importDate = LocalDateTime.now();
        }
    }
}