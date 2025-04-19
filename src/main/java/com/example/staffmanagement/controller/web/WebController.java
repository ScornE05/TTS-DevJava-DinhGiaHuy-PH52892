package com.example.staffmanagement.controller.web;

import com.example.staffmanagement.DTO.StaffDTO;
import com.example.staffmanagement.DTO.StaffSpecializationDTO;
import com.example.staffmanagement.entity.Facility;
import com.example.staffmanagement.entity.Department;
import com.example.staffmanagement.entity.Major;
import com.example.staffmanagement.repository.FacilityRepository;
import com.example.staffmanagement.repository.DepartmentRepository;
import com.example.staffmanagement.repository.MajorRepository;
import com.example.staffmanagement.service.StaffService;
import com.example.staffmanagement.service.StaffSpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebController {

    private final StaffService staffService;
    private final StaffSpecializationService staffSpecializationService;
    private final FacilityRepository facilityRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;

    @GetMapping
    public String home() {
        return "redirect:/staff";
    }

    // Danh sách nhân viên
    @GetMapping("/staff")
    public String staffList(Model model) {
        List<StaffDTO> staffList = staffService.getAllStaff();
        model.addAttribute("staffList", staffList);
        return "staff/list";
    }

    // Form thêm nhân viên
    @GetMapping("/staff/add")
    public String showAddStaffForm(Model model) {
        model.addAttribute("staff", new StaffDTO());
        return "staff/add";
    }

    // Xử lý thêm nhân viên
    @PostMapping("/staff/add")
    public String addStaff(@Valid @ModelAttribute("staff") StaffDTO staffDTO,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "staff/add";
        }

        try {
            staffService.createStaff(staffDTO);
            return "redirect:/staff?success=Thêm nhân viên thành công";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "staff/add";
        }
    }

    // Form sửa nhân viên
    @GetMapping("/staff/edit/{id}")
    public String showEditStaffForm(@PathVariable UUID id, Model model) {
        StaffDTO staffDTO = staffService.getStaffById(id);
        model.addAttribute("staff", staffDTO);
        return "staff/edit";
    }

    // Xử lý sửa nhân viên
    @PostMapping("/staff/edit/{id}")
    public String updateStaff(@PathVariable UUID id,
                              @Valid @ModelAttribute("staff") StaffDTO staffDTO,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "staff/edit";
        }

        try {
            staffService.updateStaff(id, staffDTO);
            return "redirect:/staff?success=Cập nhật nhân viên thành công";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "staff/edit";
        }
    }

    // Đổi trạng thái nhân viên
    @GetMapping("/staff/toggle/{id}")
    public String toggleStaffStatus(@PathVariable UUID id) {
        staffService.toggleStaffStatus(id);
        return "redirect:/staff?success=Đổi trạng thái nhân viên thành công";
    }

    // Quản lý bộ môn chuyên ngành
    @GetMapping("/staff/specialization/{id}")
    public String staffSpecialization(@PathVariable UUID id, Model model) {
        StaffDTO staff = staffService.getStaffById(id);
        List<StaffSpecializationDTO> specializations = staffSpecializationService.getStaffSpecializations(id);
        List<Facility> facilities = facilityRepository.findAllActive();

        model.addAttribute("staff", staff);
        model.addAttribute("specializations", specializations);
        model.addAttribute("facilities", facilities);
        model.addAttribute("specializationDTO", new StaffSpecializationDTO());
        return "staff/specialization";
    }

    // Lấy bộ môn theo cơ sở
    @GetMapping("/departments/facility/{facilityId}")
    @ResponseBody
    public List<Department> getDepartmentsByFacility(@PathVariable UUID facilityId) {
        return departmentRepository.findAllByFacilityId(facilityId);
    }

    // Lấy chuyên ngành theo bộ môn và cơ sở
    @GetMapping("/majors/department/{departmentId}/facility/{facilityId}")
    @ResponseBody
    public List<Major> getMajorsByDepartmentAndFacility(
            @PathVariable UUID departmentId, @PathVariable UUID facilityId) {
        return majorRepository.findAllByDepartmentIdAndFacilityId(departmentId, facilityId);
    }

    // Thêm bộ môn chuyên ngành cho nhân viên
    @PostMapping("/staff/specialization/add")
    public String addStaffSpecialization(@ModelAttribute StaffSpecializationDTO dto, Model model) {
        try {
            staffSpecializationService.addStaffSpecialization(dto);
            return "redirect:/staff/specialization/" + dto.getStaffId() + "?success=Thêm chuyên ngành thành công";
        } catch (Exception e) {
            return "redirect:/staff/specialization/" + dto.getStaffId() + "?error=" + e.getMessage();
        }
    }

    // Xóa bộ môn chuyên ngành của nhân viên
    @GetMapping("/staff/specialization/delete/{staffId}/{id}")
    public String removeStaffSpecialization(@PathVariable UUID staffId, @PathVariable UUID id) {
        staffSpecializationService.removeStaffSpecialization(id);
        return "redirect:/staff/specialization/" + staffId + "?success=Xóa chuyên ngành thành công";
    }
}