package com.example.staffmanagement.controller;

import com.example.staffmanagement.entity.Department;
import com.example.staffmanagement.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentRepository.findAllActive());
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<Department>> getDepartmentsByFacility(@PathVariable UUID facilityId) {
        return ResponseEntity.ok(departmentRepository.findAllByFacilityId(facilityId));
    }
}