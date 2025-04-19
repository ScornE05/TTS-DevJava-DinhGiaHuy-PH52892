package com.example.staffmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff extends BaseEntity {

    @Column(name = "staff_code")
    private String staffCode;

    @Column(name = "name")
    private String name;

    @Column(name = "account_fpt")
    private String accountFpt;

    @Column(name = "account_fe")
    private String accountFe;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StaffMajorFacility> staffMajorFacilities = new HashSet<>();

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepartmentFacility> departmentFacilities = new HashSet<>();

    public void addStaffMajorFacility(StaffMajorFacility staffMajorFacility) {
        staffMajorFacilities.add(staffMajorFacility);
        staffMajorFacility.setStaff(this);
    }

    public void removeStaffMajorFacility(StaffMajorFacility staffMajorFacility) {
        staffMajorFacilities.remove(staffMajorFacility);
        staffMajorFacility.setStaff(null);
    }
}
