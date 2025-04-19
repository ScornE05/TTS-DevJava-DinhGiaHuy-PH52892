package com.example.staffmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "department_facility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentFacility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_department")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facility")
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_staff")
    private Staff staff;

    @OneToMany(mappedBy = "departmentFacility", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MajorFacility> majorFacilities = new HashSet<>();

    // Helper methods for bidirectional relationship management
    public void addMajorFacility(MajorFacility majorFacility) {
        majorFacilities.add(majorFacility);
        majorFacility.setDepartmentFacility(this);
    }

    public void removeMajorFacility(MajorFacility majorFacility) {
        majorFacilities.remove(majorFacility);
        majorFacility.setDepartmentFacility(null);
    }
}
