package com.example.staffmanagement.service;

import com.example.staffmanagement.DTO.StaffSpecializationDTO;
import com.example.staffmanagement.entity.*;
import com.example.staffmanagement.exception.DuplicateResourceException;
import com.example.staffmanagement.exception.ResourceNotFoundException;
import com.example.staffmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffSpecializationService {

    private final StaffRepository staffRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final DepartmentFacilityRepository departmentFacilityRepository;
    private final MajorFacilityRepository majorFacilityRepository;
    private final StaffMajorFacilityRepository staffMajorFacilityRepository;

    public List<StaffSpecializationDTO> getStaffSpecializations(UUID staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại với ID: " + staffId));

        return staffMajorFacilityRepository.findAllByStaffId(staffId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffSpecializationDTO addStaffSpecialization(StaffSpecializationDTO dto) {
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại với ID: " + dto.getStaffId()));

        Facility facility = facilityRepository.findById(dto.getFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Cơ sở không tồn tại với ID: " + dto.getFacilityId()));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bộ môn không tồn tại với ID: " + dto.getDepartmentId()));

        Major major = majorRepository.findById(dto.getMajorId())
                .orElseThrow(() -> new ResourceNotFoundException("Chuyên ngành không tồn tại với ID: " + dto.getMajorId()));

        List<StaffMajorFacility> existingSpecializations = staffMajorFacilityRepository
                .findByStaffIdAndFacilityId(staff.getId(), facility.getId());

        if (!existingSpecializations.isEmpty()) {
            throw new DuplicateResourceException("Nhân viên đã có chuyên ngành trong cơ sở này");
        }

        DepartmentFacility departmentFacility = getDepartmentFacility(department, facility);

        MajorFacility majorFacility = getMajorFacility(major, departmentFacility);

        StaffMajorFacility staffMajorFacility = new StaffMajorFacility();
        staffMajorFacility.setStaff(staff);
        staffMajorFacility.setMajorFacility(majorFacility);
        staffMajorFacility.setStatus((byte) 1);

        staffMajorFacility = staffMajorFacilityRepository.save(staffMajorFacility);

        return convertToDTO(staffMajorFacility);
    }

    @Transactional
    public void removeStaffSpecialization(UUID id) {
        StaffMajorFacility staffMajorFacility = staffMajorFacilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chuyên ngành của nhân viên không tồn tại với ID: " + id));
        staffMajorFacilityRepository.deleteById(id);
    }

    private DepartmentFacility getDepartmentFacility(Department department, Facility facility) {
        Optional<DepartmentFacility> optionalDeptFacility = departmentFacilityRepository
                .findByDepartmentAndFacility(department, facility);

        if (optionalDeptFacility.isPresent()) {
            return optionalDeptFacility.get();
        } else {
            DepartmentFacility departmentFacility = new DepartmentFacility();
            departmentFacility.setDepartment(department);
            departmentFacility.setFacility(facility);
            departmentFacility.setStatus((byte) 1);
            return departmentFacilityRepository.save(departmentFacility);
        }
    }

    private MajorFacility getMajorFacility(Major major, DepartmentFacility departmentFacility) {
        Optional<MajorFacility> optionalMajorFacility = majorFacilityRepository
                .findByMajorAndDepartmentFacility(major, departmentFacility);

        if (optionalMajorFacility.isPresent()) {
            return optionalMajorFacility.get();
        } else {
            MajorFacility majorFacility = new MajorFacility();
            majorFacility.setMajor(major);
            majorFacility.setDepartmentFacility(departmentFacility);
            majorFacility.setStatus((byte) 1);
            return majorFacilityRepository.save(majorFacility);
        }
    }

    private StaffSpecializationDTO convertToDTO(StaffMajorFacility entity) {
        StaffSpecializationDTO dto = new StaffSpecializationDTO();
        dto.setId(entity.getId());
        dto.setStaffId(entity.getStaff().getId());
        dto.setStaffName(entity.getStaff().getName());

        MajorFacility majorFacility = entity.getMajorFacility();
        DepartmentFacility departmentFacility = majorFacility.getDepartmentFacility();

        dto.setMajorId(majorFacility.getMajor().getId());
        dto.setMajorName(majorFacility.getMajor().getName());

        dto.setDepartmentId(departmentFacility.getDepartment().getId());
        dto.setDepartmentName(departmentFacility.getDepartment().getName());

        dto.setFacilityId(departmentFacility.getFacility().getId());
        dto.setFacilityName(departmentFacility.getFacility().getName());

        return dto;
    }
}
