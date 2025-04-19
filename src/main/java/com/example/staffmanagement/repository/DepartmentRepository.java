package com.example.staffmanagement.repository;

import com.example.staffmanagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByCode(String code);

    @Query("SELECT d FROM Department d WHERE d.status = 1 ORDER BY d.name ASC")
    List<Department> findAllActive();

    @Query("SELECT d FROM Department d JOIN DepartmentFacility df ON d.id = df.department.id " +
            "WHERE df.facility.id = :facilityId AND d.status = 1 ORDER BY d.name ASC")
    List<Department> findAllByFacilityId(@Param("facilityId") UUID facilityId);
}