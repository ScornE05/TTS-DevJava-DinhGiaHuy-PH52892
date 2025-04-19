package com.example.staffmanagement.repository;

import com.example.staffmanagement.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {

    Optional<Facility> findByCode(String code);

    @Query("SELECT f FROM Facility f WHERE f.status = 1 ORDER BY f.name ASC")
    List<Facility> findAllActive();
}