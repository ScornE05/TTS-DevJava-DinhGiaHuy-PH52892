package com.example.staffmanagement.repository;

import com.example.staffmanagement.entity.Department;
import com.example.staffmanagement.entity.DepartmentFacility;
import com.example.staffmanagement.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentFacilityRepository extends JpaRepository<DepartmentFacility, UUID> {

    Optional<DepartmentFacility> findByDepartmentAndFacility(Department department, Facility facility);

    @Query("SELECT df FROM DepartmentFacility df " +
            "WHERE df.department.id = :departmentId AND df.facility.id = :facilityId")
    Optional<DepartmentFacility> findByDepartmentIdAndFacilityId(
            @Param("departmentId") UUID departmentId,
            @Param("facilityId") UUID facilityId);
}