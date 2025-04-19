package com.example.staffmanagement.controller;


import com.example.staffmanagement.DTO.StaffSpecializationDTO;
import com.example.staffmanagement.service.StaffSpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff-specializations")
@RequiredArgsConstructor
public class StaffSpecializationController {

    private final StaffSpecializationService staffSpecializationService;

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<StaffSpecializationDTO>> getStaffSpecializations(@PathVariable UUID staffId) {
        return ResponseEntity.ok(staffSpecializationService.getStaffSpecializations(staffId));
    }

    @PostMapping
    public ResponseEntity<StaffSpecializationDTO> addStaffSpecialization(@Valid @RequestBody StaffSpecializationDTO dto) {
        return new ResponseEntity<>(staffSpecializationService.addStaffSpecialization(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeStaffSpecialization(@PathVariable UUID id) {
        staffSpecializationService.removeStaffSpecialization(id);
        return ResponseEntity.noContent().build();
    }
}