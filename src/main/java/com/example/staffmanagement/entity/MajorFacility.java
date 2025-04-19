package com.example.staffmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "major_facility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MajorFacility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_department_facility")
    private DepartmentFacility departmentFacility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_major")
    private Major major;

    @OneToMany(mappedBy = "majorFacility", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StaffMajorFacility> staffMajorFacilities = new HashSet<>();

    public void addStaffMajorFacility(StaffMajorFacility staffMajorFacility) {
        staffMajorFacilities.add(staffMajorFacility);
        staffMajorFacility.setMajorFacility(this);
    }

    public void removeStaffMajorFacility(StaffMajorFacility staffMajorFacility) {
        staffMajorFacilities.remove(staffMajorFacility);
        staffMajorFacility.setMajorFacility(null);
    }
}