package com.example.admin.repository;

import com.example.admin.entity.AdminPasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AdminPasswordHistoryRepository extends JpaRepository<AdminPasswordHistory, UUID> {

    @Query("SELECT aph FROM AdminPasswordHistory aph WHERE aph.adminId = :adminId ORDER BY aph.createdAt DESC")
    List<AdminPasswordHistory> findRecentByAdminId(@Param("adminId") UUID adminId, Pageable pageable);
}
