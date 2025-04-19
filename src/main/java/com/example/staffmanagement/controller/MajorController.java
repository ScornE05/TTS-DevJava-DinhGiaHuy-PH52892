package com.example.staffmanagement.controller;

import com.example.staffmanagement.entity.Major;
import com.example.staffmanagement.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorController {

    private final MajorRepository majorRepository;

    @GetMapping
    public ResponseEntity<List<Major>> getAllMajors() {
        return ResponseEntity.ok(majorRepository.findAllActive());
    }

    @GetMapping("/department/{departmentId}/facility/{facilityId}")
    public ResponseEntity<List<Major>> getMajorsByDepartmentAndFacility(
            @PathVariable UUID departmentId,
            @PathVariable UUID facilityId) {
        return ResponseEntity.ok(majorRepository.findAllByDepartmentIdAndFacilityId(departmentId, facilityId));
    }
}
