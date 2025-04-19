package com.example.staffmanagement.controller;

import com.example.staffmanagement.DTO.StaffDTO;
import com.example.staffmanagement.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<StaffDTO>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @GetMapping("/active")
    public ResponseEntity<List<StaffDTO>> getAllActiveStaff() {
        return ResponseEntity.ok(staffService.getAllActiveStaff());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffDTO> getStaffById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @PostMapping
    public ResponseEntity<StaffDTO> createStaff(@Valid @RequestBody StaffDTO staffDTO) {
        return new ResponseEntity<>(staffService.createStaff(staffDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable UUID id, @Valid @RequestBody StaffDTO staffDTO) {
        return ResponseEntity.ok(staffService.updateStaff(id, staffDTO));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<StaffDTO> toggleStaffStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.toggleStaffStatus(id));
    }
}
