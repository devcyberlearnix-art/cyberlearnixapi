package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.FeeConfigAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeConfigAuditRepository extends JpaRepository<FeeConfigAudit, Long> {
    List<FeeConfigAudit> findByKeyOrderByChangedAtDesc(String key);
}
