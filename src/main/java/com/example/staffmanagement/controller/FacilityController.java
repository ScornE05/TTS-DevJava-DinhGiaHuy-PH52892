package com.example.staffmanagement.controller;

import com.example.staffmanagement.entity.Facility;
import com.example.staffmanagement.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityRepository facilityRepository;

    @GetMapping
    public ResponseEntity<List<Facility>> getAllFacilities() {
        return ResponseEntity.ok(facilityRepository.findAllActive());
    }
}