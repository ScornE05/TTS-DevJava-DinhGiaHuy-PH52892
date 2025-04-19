package com.example.staffmanagement.service;

import com.example.staffmanagement.DTO.StaffDTO;
import com.example.staffmanagement.entity.Staff;
import com.example.staffmanagement.exception.ResourceAlreadyExistsException;
import com.example.staffmanagement.exception.ResourceNotFoundException;
import com.example.staffmanagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;

    private final Pattern FPT_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@fpt\\.edu\\.vn$");
    private final Pattern FE_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@fe\\.edu\\.vn$");

    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAllOrderByName().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StaffDTO> getAllActiveStaff() {
        return staffRepository.findAllByStatus((byte) 1).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StaffDTO getStaffById(UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại với ID: " + id));
        return convertToDTO(staff);
    }

    @Transactional
    public StaffDTO createStaff(StaffDTO staffDTO) {
        validateStaffData(staffDTO, null);
        Staff staff = convertToEntity(staffDTO);
        staff.setStatus((byte) 1);
        Staff savedStaff = staffRepository.save(staff);
        return convertToDTO(savedStaff);
    }

    @Transactional
    public StaffDTO updateStaff(UUID id, StaffDTO staffDTO) {
        Staff existingStaff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại với ID: " + id));
        validateStaffData(staffDTO, id);
        existingStaff.setName(staffDTO.getName());
        existingStaff.setStaffCode(staffDTO.getStaffCode());
        existingStaff.setAccountFpt(staffDTO.getAccountFpt());
        existingStaff.setAccountFe(staffDTO.getAccountFe());
        Staff updatedStaff = staffRepository.save(existingStaff);
        return convertToDTO(updatedStaff);
    }

    @Transactional
    public StaffDTO toggleStaffStatus(UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại với ID: " + id));
        staff.setStatus(staff.getStatus() == 1 ? (byte) 0 : (byte) 1);
        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    private void validateStaffData(StaffDTO staffDTO, UUID id) {
        // Kiểm tra định dạng email
        if (!FPT_EMAIL_PATTERN.matcher(staffDTO.getAccountFpt()).matches()) {
            throw new IllegalArgumentException("Email FPT phải có định dạng xxx@fpt.edu.vn và không được chứa khoảng trắng hoặc ký tự tiếng Việt");
        }

        if (!FE_EMAIL_PATTERN.matcher(staffDTO.getAccountFe()).matches()) {
            throw new IllegalArgumentException("Email FE phải có định dạng xxx@fe.edu.vn và không được chứa khoảng trắng hoặc ký tự tiếng Việt");
        }

        // Kiểm tra email phải chứa mã nhân viên
        String staffCodeLower = staffDTO.getStaffCode().toLowerCase();
        if (!staffDTO.getAccountFpt().toLowerCase().contains(staffCodeLower)) {
            throw new IllegalArgumentException("Email FPT phải chứa mã nhân viên");
        }

        if (!staffDTO.getAccountFe().toLowerCase().contains(staffCodeLower)) {
            throw new IllegalArgumentException("Email FE phải chứa mã nhân viên");
        }

        // Kiểm tra trùng
        validateUniqueConstraints(staffDTO, id);
    }

    private void validateUniqueConstraints(StaffDTO staffDTO, UUID id) {
        if (id == null) {
            if (staffRepository.existsByStaffCode(staffDTO.getStaffCode())) {
                throw new ResourceAlreadyExistsException("Mã nhân viên đã tồn tại: " + staffDTO.getStaffCode());
            }

            if (staffRepository.existsByAccountFpt(staffDTO.getAccountFpt())) {
                throw new ResourceAlreadyExistsException("Email FPT đã tồn tại: " + staffDTO.getAccountFpt());
            }

            if (staffRepository.existsByAccountFe(staffDTO.getAccountFe())) {
                throw new ResourceAlreadyExistsException("Email FE đã tồn tại: " + staffDTO.getAccountFe());
            }
        } else {
            if (staffRepository.existsByStaffCodeAndIdNot(staffDTO.getStaffCode(), id)) {
                throw new ResourceAlreadyExistsException("Mã nhân viên đã tồn tại: " + staffDTO.getStaffCode());
            }

            if (staffRepository.existsByAccountFptAndIdNot(staffDTO.getAccountFpt(), id)) {
                throw new ResourceAlreadyExistsException("Email FPT đã tồn tại: " + staffDTO.getAccountFpt());
            }

            if (staffRepository.existsByAccountFeAndIdNot(staffDTO.getAccountFe(), id)) {
                throw new ResourceAlreadyExistsException("Email FE đã tồn tại: " + staffDTO.getAccountFe());
            }
        }
    }

    private StaffDTO convertToDTO(Staff staff) {
        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setStaffCode(staff.getStaffCode());
        dto.setName(staff.getName());
        dto.setAccountFpt(staff.getAccountFpt());
        dto.setAccountFe(staff.getAccountFe());
        dto.setStatus(staff.getStatus());
        return dto;
    }

    private Staff convertToEntity(StaffDTO dto) {
        Staff staff = new Staff();
        staff.setId(dto.getId());
        staff.setStaffCode(dto.getStaffCode());
        staff.setName(dto.getName());
        staff.setAccountFpt(dto.getAccountFpt());
        staff.setAccountFe(dto.getAccountFe());
        staff.setStatus(dto.getStatus());
        return staff;
    }
}