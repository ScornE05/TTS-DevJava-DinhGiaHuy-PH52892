package com.example.staffmanagement.repository;

import com.example.staffmanagement.entity.DepartmentFacility;
import com.example.staffmanagement.entity.Major;
import com.example.staffmanagement.entity.MajorFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MajorFacilityRepository extends JpaRepository<MajorFacility, UUID> {

    Optional<MajorFacility> findByMajorAndDepartmentFacility(Major major, DepartmentFacility departmentFacility);

    @Query("SELECT mf FROM MajorFacility mf " +
            "WHERE mf.major.id = :majorId AND mf.departmentFacility.id = :departmentFacilityId")
    Optional<MajorFacility> findByMajorIdAndDepartmentFacilityId(
            @Param("majorId") UUID majorId,
            @Param("departmentFacilityId") UUID departmentFacilityId);
}