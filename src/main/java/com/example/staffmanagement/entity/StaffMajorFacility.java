package com.example.staffmanagement.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "staff_major_facility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffMajorFacility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_staff")
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_major_facility")
    private MajorFacility majorFacility;
}
