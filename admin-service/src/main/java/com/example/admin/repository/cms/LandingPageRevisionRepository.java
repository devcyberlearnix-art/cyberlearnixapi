package com.example.admin.repository.cms;

import com.example.admin.entity.cms.LandingPageRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandingPageRevisionRepository extends JpaRepository<LandingPageRevision, UUID> {
    List<LandingPageRevision> findByPageKeyOrderByVersionNumberDesc(String pageKey);
    Optional<LandingPageRevision> findTopByPageKeyOrderByVersionNumberDesc(String pageKey);
}

