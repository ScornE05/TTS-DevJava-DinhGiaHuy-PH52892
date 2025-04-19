package com.example.staffmanagement.repository;

import com.example.staffmanagement.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {

    Optional<Staff> findByStaffCode(String staffCode);

    Optional<Staff> findByAccountFpt(String accountFpt);

    Optional<Staff> findByAccountFe(String accountFe);

    boolean existsByStaffCode(String staffCode);

    boolean existsByAccountFpt(String accountFpt);

    boolean existsByAccountFe(String accountFe);

    boolean existsByStaffCodeAndIdNot(String staffCode, UUID id);

    boolean existsByAccountFptAndIdNot(String accountFpt, UUID id);

    boolean existsByAccountFeAndIdNot(String accountFe, UUID id);

    @Query("SELECT s FROM Staff s WHERE s.status = :status")
    List<Staff> findAllByStatus(@Param("status") Byte status);

    @Query("SELECT s FROM Staff s ORDER BY s.name ASC")
    List<Staff> findAllOrderByName();
}