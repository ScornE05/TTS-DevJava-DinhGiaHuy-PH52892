package com.example.staffmanagement.repository;

import com.example.staffmanagement.entity.StaffMajorFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffMajorFacilityRepository extends JpaRepository<StaffMajorFacility, UUID> {

    @Query("SELECT smf FROM StaffMajorFacility smf " +
            "JOIN smf.majorFacility mf " +
            "JOIN mf.departmentFacility df " +
            "WHERE smf.staff.id = :staffId AND df.facility.id = :facilityId")
    List<StaffMajorFacility> findByStaffIdAndFacilityId(
            @Param("staffId") UUID staffId,
            @Param("facilityId") UUID facilityId);

    @Query("SELECT smf FROM StaffMajorFacility smf " +
            "JOIN smf.majorFacility mf " +
            "JOIN mf.departmentFacility df " +
            "WHERE smf.staff.id = :staffId AND df.department.id = :departmentId " +
            "AND df.facility.id = :facilityId")
    Optional<StaffMajorFacility> findByStaffIdAndDepartmentIdAndFacilityId(
            @Param("staffId") UUID staffId,
            @Param("departmentId") UUID departmentId,
            @Param("facilityId") UUID facilityId);

    @Query("SELECT smf FROM StaffMajorFacility smf " +
            "WHERE smf.staff.id = :staffId")
    List<StaffMajorFacility> findAllByStaffId(@Param("staffId") UUID staffId);

    void deleteById(UUID id);
}