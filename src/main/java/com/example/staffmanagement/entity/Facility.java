package com.example.staffmanagement.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "facility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Facility extends BaseEntity {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepartmentFacility> departmentFacilities = new HashSet<>();
}
