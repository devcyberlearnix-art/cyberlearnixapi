package com.example.admin.repository;

import com.example.admin.entity.Admin;
import com.example.admin.entity.AdminType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByEmail(String email);

    List<Admin> findByAdminType(AdminType adminType);
}
