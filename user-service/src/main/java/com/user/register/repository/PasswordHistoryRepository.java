package com.user.register.repository;

import com.user.register.entity.PasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.userId = :userId ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);
}
